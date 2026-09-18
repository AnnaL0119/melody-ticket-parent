<template>
  <div>
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2 class="logo">LinMelodyTicket 管理后台</h2>
          <el-menu mode="horizontal" router :ellipsis="false">
            <el-menu-item index="/">演唱会列表</el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/music">音乐播放器</el-menu-item>
            <el-menu-item index="/admin/concerts">演唱会管理</el-menu-item>
          </el-menu>
          <div class="user-area">
            <el-dropdown trigger="click">
              <span class="user-dropdown">
                <el-avatar :size="32" :icon="UserFilled" class="user-avatar" />
                <span>管理员</span>
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
        <div class="toolbar">
          <h2>演唱会管理</h2>
          <el-button type="primary" @click="showDialog(null)">新增演唱会</el-button>
        </div>

        <el-table :data="concerts" stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="名称" width="180" />
          <el-table-column prop="singer" label="歌手" width="120" />
          <el-table-column prop="venue" label="场馆" width="150" />
          <el-table-column prop="showTime" label="演出时间" width="160" />
          <el-table-column label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="余票/总票" width="120">
            <template #default="{ row }">{{ row.remainingStock }}/{{ row.totalStock }}</template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="showDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && concerts.length === 0" description="暂无演唱会" />
      </el-main>

      <el-footer class="page-footer">
        <span>LinMelodyTicket 旋律票务系统 &copy; 2026</span>
      </el-footer>
    </el-container>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="歌手">
          <el-input v-model="form.singer" />
        </el-form-item>
        <el-form-item label="场馆">
          <el-input v-model="form.venue" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="演出时间">
          <el-date-picker v-model="form.showTime" type="datetime" format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总库存">
          <el-input-number v-model="form.totalStock" :min="1" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverUrl" placeholder="图片URL（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConcert" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const concerts = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const dialogTitle = ref('新增演唱会')

const form = ref({ name: '', singer: '', venue: '', city: '', showTime: '', totalStock: 100, price: 688, coverUrl: '' })

onMounted(() => { loadConcerts() })

async function loadConcerts() {
  loading.value = true
  try {
    const res = await request.get('/concerts')
    if (res.code === 200) concerts.value = res.data || []
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function showDialog(concert) {
  if (concert) {
    editingId.value = concert.id
    dialogTitle.value = '编辑演唱会'
    form.value = { ...concert }
  } else {
    editingId.value = null
    dialogTitle.value = '新增演唱会'
    form.value = { name: '', singer: '', venue: '', city: '', showTime: '', totalStock: 100, price: 688, coverUrl: '' }
  }
  dialogVisible.value = true
}

async function saveConcert() {
  saving.value = true
  try {
    let res
    if (editingId.value) {
      res = await request.put(`/concerts/${editingId.value}`, form.value)
    } else {
      res = await request.post('/concerts', form.value)
    }
    if (res.code === 200) {
      ElMessage.success(editingId.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadConcerts()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该演唱会吗？', '确认删除', { type: 'warning' })
    const res = await request.delete(`/concerts/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadConcerts()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function logout() { localStorage.clear(); router.push('/login') }
</script>

<style scoped>
.header { background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.header-content { display: flex; align-items: center; height: 60px; }
.logo { color: #d6336c; margin-right: 40px; }
.user-area { margin-left: auto; }
.user-dropdown { display: flex; align-items: center; gap: 6px; cursor: pointer; color: #d6336c; }
.user-avatar { background: #f48fb1; }
.page-footer { text-align: center; color: #bbb; font-size: 13px; line-height: 60px; margin-top: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
</style>
