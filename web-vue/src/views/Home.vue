<template>
  <div>
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2 class="logo">LinMelodyTicket</h2>
          <el-menu mode="horizontal" :ellipsis="false" router>
            <el-menu-item index="/">演唱会列表</el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/music">音乐播放器</el-menu-item>
            <el-menu-item v-if="isAdmin" index="/admin/concerts">管理后台</el-menu-item>
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
        <div class="search-bar">
          <el-input v-model="searchQuery" placeholder="搜索演唱会名称或歌手" clearable style="width: 400px">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="concert in filteredConcerts" :key="concert.id">
            <el-card class="concert-card" :body-style="{ padding: '0' }" shadow="hover" @click="goDetail(concert.id)">
              <img :src="concert.coverUrl || 'https://picsum.photos/400/200?music'" class="concert-image" />
              <div class="concert-info">
                <h3>{{ concert.name }}</h3>
                <p class="singer">{{ concert.singer }}</p>
                <div class="meta">
                  <span>{{ concert.venue }}</span>
                  <span>{{ concert.showTime?.substring(0, 10) }}</span>
                </div>
                <div class="footer">
                  <span class="price">¥{{ concert.price }}</span>
                  <span class="stock" :class="{ low: concert.remainingStock <= 10 }">
                    余票 {{ concert.remainingStock }}
                  </span>
                </div>
                <el-button type="danger" style="width: 100%; margin-top: 10px" @click.stop="buyNow(concert)">
                  立即购买
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="filteredConcerts.length === 0" description="暂无演唱会信息" />
      </el-main>
      <el-footer class="page-footer">
        <span>LinMelodyTicket 旋律票务系统 &copy; 2026</span>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const concerts = ref([])
const searchQuery = ref('')

const username = computed(() => localStorage.getItem('username') || '用户')
const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')

const filteredConcerts = computed(() => {
  if (!searchQuery.value) return concerts.value
  const q = searchQuery.value.toLowerCase()
  return concerts.value.filter(c =>
    c.name?.toLowerCase().includes(q) || c.singer?.toLowerCase().includes(q)
  )
})

onMounted(() => { loadConcerts() })

async function loadConcerts() {
  try {
    const res = await request.get('/concerts')
    if (res.code === 200) concerts.value = res.data || []
  } catch (e) { /* handled */ }
}

function goDetail(id) { router.push(`/concert/${id}`) }

async function buyNow(concert) {
  const userId = localStorage.getItem('userId')
  if (!userId) { ElMessage.warning('请先登录'); return }
  try {
    const res = await request.post('/tickets/orders', {
      userId: parseInt(userId),
      concertId: concert.id,
      quantity: 1
    })
    if (res.code === 200) {
      ElMessage.success('购票成功！')
      loadConcerts()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { /* handled */ }
}

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.header-content { display: flex; align-items: center; height: 60px; padding: 0 20px; }
.logo { color: #d6336c; margin-right: 40px; }
.user-area { margin-left: auto; }
.user-dropdown { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #d6336c; }
.user-avatar { background: #f48fb1; }
.search-bar { text-align: center; margin: 20px 0; }
.page-footer { text-align: center; color: #bbb; font-size: 13px; line-height: 60px; margin-top: 20px; }
.concert-card { margin-bottom: 20px; cursor: pointer; border-radius: 12px; overflow: hidden; }
.concert-image { width: 100%; height: 180px; object-fit: cover; }
.concert-info { padding: 15px; }
.concert-info h3 { margin: 0 0 5px; font-size: 18px; }
.singer { color: #f06292; margin-bottom: 10px; }
.meta { display: flex; justify-content: space-between; color: #999; font-size: 13px; margin-bottom: 10px; }
.footer { display: flex; justify-content: space-between; align-items: center; }
.price { color: #e74c3c; font-size: 20px; font-weight: bold; }
.stock { color: #27ae60; font-size: 13px; }
.stock.low { color: #e74c3c; }
</style>
