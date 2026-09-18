-- 秒杀预扣库存 Lua 脚本
-- KEYS[1]: 用户下单标记 seckill:user:{concertId}:{userId}
-- KEYS[2]: 库存 key seckill:stock:{concertId}
-- ARGV[1]: 购买数量
-- ARGV[2]: 订单排队号
-- 返回值: {success, message}

local user_key = KEYS[1]
local stock_key = KEYS[2]
local quantity = tonumber(ARGV[1])
local order_no = ARGV[2]

-- 1. 检查是否重复下单
local user_ordered = redis.call('exists', user_key)
if user_ordered == 1 then
    return {0, "您已参与过该演唱会的秒杀，请勿重复下单"}
end

-- 2. 检查库存
local stock_str = redis.call('get', stock_key)
local stock = tonumber(stock_str)
if not stock or stock < quantity then
    return {0, "库存不足"}
end

-- 3. 原子扣减库存
redis.call('decrby', stock_key, quantity)

-- 4. 标记用户已下单（24小时过期）
redis.call('setex', user_key, 86400, order_no)

return {1, "排队成功", order_no}
