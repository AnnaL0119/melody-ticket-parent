package com.melody.concert.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melody.common.exception.BizException;
import com.melody.concert.dto.ConcertDTO;
import com.melody.concert.entity.Concert;
import com.melody.concert.mapper.ConcertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertService extends ServiceImpl<ConcertMapper, Concert> {

    private final StringRedisTemplate redisTemplate;
    private final ConcertMapper concertMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有上架的演唱会
     */
    public List<ConcertDTO> getAvailableConcerts() {
        LambdaQueryWrapper<Concert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Concert::getStatus, 1);
        wrapper.orderByDesc(Concert::getShowTime);

        return this.list(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取演唱会详情（带缓存）
     */
    public ConcertDTO getConcertById(Long id) {
        String cacheKey = "concert:" + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ConcertDTO.class);
            } catch (JsonProcessingException e) {
                redisTemplate.delete(cacheKey);
            }
        }

        ConcertDTO dto = getConcertFromDB(id);
        if (dto != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(dto), 10, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                // ignore
            }
        }
        return dto;
    }

    private ConcertDTO getConcertFromDB(Long id) {
        Concert concert = this.getById(id);
        if (concert == null) {
            throw new BizException("演唱会不存在");
        }
        return convertToDTO(concert);
    }

    /**
     * 查询余票
     */
    public Integer getRemainingStock(Long id) {
        Concert concert = this.getById(id);
        if (concert == null) {
            throw new BizException("演唱会不存在");
        }
        return concert.getRemainingStock();
    }

    /**
     * 扣减库存（供 ticket-service 调用）
     * 幂等设计：以订单号 bizNo 作为幂等键，消费重试/网络重发时同一订单只扣减一次
     */
    public boolean decreaseStock(Long concertId, Integer quantity, String bizNo) {
        if (bizNo != null && !bizNo.isBlank()) {
            Boolean firstDeduct = redisTemplate.opsForValue()
                    .setIfAbsent("stock:deduct:" + bizNo, "1", 24, TimeUnit.HOURS);
            if (!Boolean.TRUE.equals(firstDeduct)) {
                log.info("重复扣减请求，幂等拦截: bizNo={}, concertId={}", bizNo, concertId);
                return true;
            }
        }

        int result = concertMapper.decreaseStock(concertId, quantity);
        if (result > 0) {
            String cacheKey = "concert:stock:" + concertId;
            redisTemplate.opsForValue().decrement(cacheKey, quantity);
            redisTemplate.delete("concert:" + concertId);
            return true;
        }
        // 库存不足：释放幂等标记，允许后续合法请求重新扣减
        if (bizNo != null && !bizNo.isBlank()) {
            redisTemplate.delete("stock:deduct:" + bizNo);
        }
        return false;
    }

    public void increaseStock(Long concertId, Integer quantity) {
        concertMapper.increaseStock(concertId, quantity);
        String cacheKey = "concert:stock:" + concertId;
        redisTemplate.opsForValue().increment(cacheKey, quantity);
        redisTemplate.delete("concert:" + concertId);
    }

    public ConcertDTO createConcert(ConcertDTO dto) {
        Concert concert = new Concert();
        BeanUtils.copyProperties(dto, concert);
        concert.setRemainingStock(dto.getTotalStock());
        concert.setStatus(1);
        this.save(concert);
        return convertToDTO(concert);
    }

    public ConcertDTO updateConcert(Long id, ConcertDTO dto) {
        Concert concert = this.getById(id);
        if (concert == null) {
            throw new BizException("演唱会不存在");
        }
        BeanUtils.copyProperties(dto, concert);
        concert.setId(id);
        this.updateById(concert);
        // 清除缓存
        redisTemplate.delete("concert:" + id);
        return convertToDTO(concert);
    }

    public void deleteConcert(Long id) {
        Concert concert = this.getById(id);
        if (concert == null) {
            throw new BizException("演唱会不存在");
        }
        concert.setStatus(0);
        this.updateById(concert);
        redisTemplate.delete("concert:" + id);
    }

    private ConcertDTO convertToDTO(Concert concert) {
        if (concert == null) return null;
        ConcertDTO dto = new ConcertDTO();
        BeanUtils.copyProperties(concert, dto);
        return dto;
    }
}