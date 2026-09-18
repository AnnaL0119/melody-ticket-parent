<template>
  <div>
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2 class="logo">LinMelodyTicket</h2>
          <el-menu mode="horizontal" router :ellipsis="false">
            <el-menu-item index="/">演唱会列表</el-menu-item>
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
        <div class="page-header">
          <h2 class="page-title">我的订单</h2>
          <el-tag type="info" effect="plain" size="small">共 {{ orders.length }} 笔</el-tag>
        </div>

        <el-table :data="orders" stripe style="width: 100%" v-loading="loading" empty-text="暂无订单">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column label="演唱会" min-width="140">
            <template #default="{ row }">{{ row.tickets?.[0]?.concertName || '-' }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="60" align="center" />
          <el-table-column label="总金额" width="100" align="center">
            <template #default="{ row }">¥{{ row.totalAmount }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" effect="dark" size="small">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="150" />
          <el-table-column label="支付时间" width="150">
            <template #default="{ row }">{{ row.payTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" type="primary" size="small" @click="payOrder(row)">去支付</el-button>
              <el-button v-if="row.status === 0" type="warning" size="small" @click="cancelOrder(row.id)">取消</el-button>
              <el-button v-if="row.status === 1 && row.tickets?.length" type="success" size="small" @click="showTicket(row)">电子票</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && orders.length === 0" description="暂无订单，去选购演唱会吧" />
      </el-main>

      <el-footer class="footer">
        <span>LinMelodyTicket 旋律票务系统 &copy; 2026</span>
      </el-footer>
    </el-container>

    <el-dialog v-model="ticketDialog.visible" title="电子票" width="500px" top="8vh" destroy-on-close>
      <div v-if="ticketDialog.tickets?.length" class="ticket-list">
        <div v-for="(ticket, idx) in ticketDialog.tickets" :key="ticket.id" class="ticket-card">
          <div class="ticket-card-header">
            <span class="ticket-badge">#{{ idx + 1 }}</span>
            <span class="ticket-concert">{{ ticketDialog.concertName }}</span>
          </div>
          <div class="ticket-card-body">
            <div class="ticket-card-info">
              <p><label>票号</label>{{ ticket.ticketNo }}</p>
              <p><label>状态</label>
                <el-tag :type="ticket.status === 0 ? 'success' : 'info'" size="small" effect="dark">
                  {{ ticket.status === 0 ? '未使用' : ticket.status === 1 ? '已使用' : '已过期' }}
                </el-tag>
              </p>
              <p v-if="ticket.seatInfo"><label>座位</label>{{ ticket.seatInfo }}</p>
            </div>
            <div class="ticket-card-qr">
              <img :src="qrUrl(ticket.qrCode)" alt="二维码" />
              <p class="qr-hint">检票时出示</p>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-ticket">
        <el-empty description="暂无电子票信息" />
      </div>
      <template #footer>
        <el-button type="primary" @click="ticketDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const orders = ref([])
const loading = ref(false)

const username = computed(() => localStorage.getItem('username') || '用户')

const ticketDialog = reactive({
  visible: false,
  concertName: '',
  tickets: []
})

onMounted(() => { loadOrders() })

async function loadOrders() {
  const userId = localStorage.getItem('userId')
  if (!userId) { router.push('/login'); return }
  loading.value = true
  try {
    const res = await request.get(`/tickets/orders/user/${userId}`)
    if (res.code === 200) orders.value = res.data || []
    else ElMessage.error(res.message)
  } catch (e) {
    ElMessage.error(e.message || '加载订单失败')
  }
  finally { loading.value = false }
}

async function payOrder(row) {
  const userId = localStorage.getItem('userId')
  try {
    await ElMessageBox.confirm(`确认支付 ¥${row.totalAmount}？`, '模拟支付')
    const res = await request.put(`/tickets/orders/${row.id}/pay?userId=${userId}`)
    if (res.code === 200) {
      ElMessage.success('支付成功，电子票已生成')
      loadOrders()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('支付失败')
  }
}

async function cancelOrder(orderId) {
  const userId = localStorage.getItem('userId')
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '确认取消')
    const res = await request.put(`/tickets/orders/${orderId}/cancel?userId=${userId}`)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

function showTicket(row) {
  ticketDialog.concertName = row.tickets?.[0]?.concertName || '演唱会'
  ticketDialog.tickets = row.tickets || []
  ticketDialog.visible = true
}

function qrUrl(code) {
  if (!code) return ''
  return `https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=${encodeURIComponent(code)}`
}

function statusText(status) {
  return ({ 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' })[status] || '未知'
}

function statusType(status) {
  return ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' })[status] || 'info'
}

function logout() { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.header { background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.header-content { display: flex; align-items: center; height: 60px; padding: 0 20px; }
.logo { color: #d6336c; margin-right: 40px; font-size: 20px; }
.user-area { margin-left: auto; }
.user-dropdown { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #d6336c; }
.user-avatar { background: #f48fb1; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.page-title { margin: 0; }

.footer {
  text-align: center;
  color: #bbb;
  font-size: 13px;
  line-height: 60px;
  margin-top: 20px;
}

.ticket-list { display: flex; flex-direction: column; gap: 16px; }

.ticket-card {
  border: 2px dashed #f48fb1;
  border-radius: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fdf2f8 0%, #fce4ec 100%);
}
.ticket-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.ticket-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f48fb1;
  color: white;
  font-size: 13px;
  font-weight: bold;
}
.ticket-concert { color: #d6336c; font-size: 16px; font-weight: 600; }

.ticket-card-body { display: flex; gap: 16px; align-items: center; }
.ticket-card-info { flex: 1; }
.ticket-card-info p { margin: 6px 0; color: #555; font-size: 13px; }
.ticket-card-info label { color: #d6336c; font-weight: 500; margin-right: 6px; }

.ticket-card-qr { text-align: center; }
.ticket-card-qr img { width: 120px; height: 120px; border: 2px solid #f48fb1; border-radius: 8px; padding: 4px; background: white; }
.qr-hint { color: #999; font-size: 11px; margin-top: 4px; }

.empty-ticket { padding: 20px 0; }
</style>
