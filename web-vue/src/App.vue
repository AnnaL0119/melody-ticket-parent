<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

onMounted(() => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (!token) {
    router.push('/login')
  }
})
</script>

<style>
:root {
  --el-color-primary: #f48fb1;
  --el-color-primary-light-3: #f8bbd0;
  --el-color-primary-light-5: #fce4ec;
  --el-color-primary-dark-2: #f06292;
}
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Microsoft YaHei', sans-serif; background: #fdf2f8; }
</style>
