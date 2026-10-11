package com.melody.concert.controller;

import com.melody.common.Result;
import com.melody.concert.dto.ConcertDTO;
import com.melody.concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping
    public Result<List<ConcertDTO>> getAvailableConcerts() {
        return Result.success(concertService.getAvailableConcerts());
    }

    @GetMapping("/{id}")
    public Result<ConcertDTO> getConcertById(@PathVariable("id") Long id) {
        return Result.success(concertService.getConcertById(id));
    }

    @GetMapping("/{id}/stock")
    public Result<Integer> getRemainingStock(@PathVariable("id") Long id) {
        return Result.success(concertService.getRemainingStock(id));
    }

    // 关键：必须在 @RequestParam 中指定 name
    @GetMapping("/decrease-stock")
    public Result<Boolean> decreaseStock(
            @RequestParam(name = "concertId") Long concertId,
            @RequestParam(name = "quantity") Integer quantity,
            @RequestParam(name = "bizNo", required = false) String bizNo) {
        boolean result = concertService.decreaseStock(concertId, quantity, bizNo);
        return result ? Result.success(true) : Result.error(400, "库存不足");
    }

    @GetMapping("/increase-stock")
    public Result<Boolean> increaseStock(
            @RequestParam(name = "concertId") Long concertId,
            @RequestParam(name = "quantity") Integer quantity) {
        concertService.increaseStock(concertId, quantity);
        return Result.success(true);
    }

    @PostMapping
    public Result<ConcertDTO> createConcert(@RequestBody ConcertDTO dto) {
        ConcertDTO created = concertService.createConcert(dto);
        return Result.success("创建成功", created);
    }

    @PutMapping("/{id}")
    public Result<ConcertDTO> updateConcert(@PathVariable("id") Long id, @RequestBody ConcertDTO dto) {
        ConcertDTO updated = concertService.updateConcert(id, dto);
        return Result.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteConcert(@PathVariable("id") Long id) {
        concertService.deleteConcert(id);
        return Result.success("删除成功");
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("concert-service is running!");
    }
}