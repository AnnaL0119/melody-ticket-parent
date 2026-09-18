<template>
  <div>
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2 class="logo">LinLinMelodyTicket</h2>
          <el-menu mode="horizontal" router :ellipsis="false">
            <el-menu-item index="/">返回列表</el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/music">音乐播放器</el-menu-item>
          </el-menu>
          <div class="user-area">
            <el-dropdown trigger="click">
              <span class="user-dropdown">
                <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
                <span>{{ username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-item @click="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main>
        <el-card v-if="concert" class="detail-card">
          <el-row :gutter="30">
            <el-col :xs="24" :md="10">
              <img :src="concert.coverUrl || 'https://picsum.photos/500/300?music'" class="detail-image" />
            </el-col>
            <el-col :xs="24" :md="14">
              <h1 class="concert-name">{{ concert.name }}</h1>
              <p class="concert-singer">{{ concert.singer }}</p>

              <el-descriptions :column="1" border>
                <el-descriptions-item label="场馆">{{ concert.venue }}</el-descriptions-item>
                <el-descriptions-item label="城市">{{ concert.city }}</el-descriptions-item>
                <el-descriptions-item label="演出时间">{{ concert.showTime?.substring(0, 16) }}</el-descriptions-item>
                <el-descriptions-item label="价格">
                  <span class="price">¥{{ concert.price }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="余票">
                  <span :class="stockClass">{{ concert.remainingStock }} 张</span>
                </el-descriptions-item>
                <el-descriptions-item label="总库存">{{ concert.totalStock }} 张</el-descriptions-item>
              </el-descriptions>

              <div class="buy-section">
                <el-input-number v-model="quantity" :min="1" :max="Math.min(concert.remainingStock, 10)" />
                <el-button type="danger" size="large" @click="handleBuy" :disabled="concert.remainingStock <= 0" :loading="buying">
                  立即购买
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-card>
        <el-empty v-else-if="!loading" description="演唱会不存在" />
      </el-main>

      <el-footer class="page-footer">
        <span>LinMelodyTicket 旋律票务系统 &copy; 2026</span>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import request from '../utils/request'

const route = useRoute()
const router = useRouter()
const concert = ref(null)
const loading = ref(true)
const buying = ref(false)
const quantity = ref(1)
const username = computed(() => localStorage.getItem('username') || '用户')

const stockClass = computed(() => ({
  'stock-ok': concert.value?.remainingStock > 10,
  'stock-low': concert.value?.remainingStock > 0 && concert.value?.remainingStock <= 10,
  'stock-none': concert.value?.remainingStock <= 0
}))

onMounted(() => { loadConcert() })

async function loadConcert() {
  try {
    const res = await request.get(`/concerts/${route.params.id}`)
    if (res.code === 200) concert.value = res.data
    else ElMessage.error(res.message || '获取演唱会信息失败')
  } catch (e) {
    ElMessage.error(e.message || '获取演唱会信息失败')
  }
  finally { loading.value = false }
}

async function handleBuy() {
  const userId = localStorage.getItem('userId')
  if (!userId) { ElMessage.warning('请先登录'); router.push('/login'); return }

  buying.value = true
  try {
    const res = await request.post('/tickets/orders', {
      userId: parseInt(userId),
      concertId: concert.value.id,
      quantity: quantity.value
    })
    if (res.code === 200) {
      ElMessage.success('购票成功！')
      loadConcert()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.message || '系统错误，请稍后重试')
  } finally { buying.value = false }
}

function logout() { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.header { background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.header-content { display: flex; align-items: center; height: 60px; padding: 0 20px; }
.logo { color: #d6336c; margin-right: 40px; }
.user-area { margin-left: auto; }
.user-dropdown { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #d6336c; }
.user-avatar { background: #f48fb1; }
.page-footer { text-align: center; color: #bbb; font-size: 13px; line-height: 60px; margin-top: 20px; }
.detail-card { margin-top: 20px; }
.detail-image { width: 100%; border-radius: 12px; }
.concert-name { font-size: 28px; margin-bottom: 5px; }
.concert-singer { color: #f06292; font-size: 18px; margin-bottom: 20px; }
.price { color: #e74c3c; font-size: 22px; font-weight: bold; }
.stock-ok { color: #27ae60; font-weight: bold; }
.stock-low { color: #e67e22; font-weight: bold; }
.stock-none { color: #e74c3c; font-weight: bold; }
.buy-section { margin-top: 30px; display: flex; align-items: center; gap: 20px; }
</style>
