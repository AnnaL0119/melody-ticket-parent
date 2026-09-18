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
        <div class="player-header">
          <h2>音乐播放器</h2>
          <p class="player-subtitle">在购票之余，听听音乐放松一下吧</p>
        </div>

        <el-row :gutter="20">
          <el-col :xs="24" :md="14">
            <div class="current-player" v-if="currentSong">
              <div class="player-card">
                <div class="player-cover" :class="{ playing: isPlaying }">
                  <span class="cover-icon">&#9835;</span>
                </div>
                <div class="player-info">
                  <h3>{{ currentSong.name }}</h3>
                  <p>{{ currentSong.artist }}</p>
                  <div class="player-controls">
                    <el-button circle size="small" @click="playPrev" :disabled="currentIndex <= 0">
                      <el-icon><DArrowLeft /></el-icon>
                    </el-button>
                    <el-button circle type="primary" size="large" @click="togglePlay">
                      <el-icon><VideoPlay v-if="!isPlaying" /><VideoPause v-else /></el-icon>
                    </el-button>
                    <el-button circle size="small" @click="playNext" :disabled="currentIndex >= songs.length - 1">
                      <el-icon><DArrowRight /></el-icon>
                    </el-button>
                  </div>
                  <div class="progress-bar">
                    <span class="time">{{ formatTime(currentTime) }}</span>
                    <el-slider v-model="sliderValue" :max="duration" size="small" @change="seekTo" :show-tooltip="false" />
                    <span class="time">{{ formatTime(duration) }}</span>
                  </div>

                </div>
              </div>
            </div>
            <div v-else class="empty-player">
              <el-empty description="请选择一首歌曲播放" />
            </div>
          </el-col>

          <el-col :xs="24" :md="10">
            <div class="playlist">
              <h3 class="playlist-title">歌曲列表</h3>
              <div class="song-list">
                <div
                  v-for="(song, idx) in songs"
                  :key="idx"
                  class="song-item"
                  :class="{ active: currentIndex === idx }"
                  @click="selectSong(idx)"
                >
                  <span class="song-idx">{{ currentIndex === idx ? '&#9835;' : idx + 1 }}</span>
                  <div class="song-info">
                    <span class="song-name">{{ song.name }}</span>
                    <span class="song-artist">{{ song.artist }}</span>
                  </div>
                  <el-icon v-if="currentIndex === idx && isPlaying" class="playing-indicator"><VideoPlay /></el-icon>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-main>

      <el-footer class="page-footer">
        <span>LinMelodyTicket 旋律票务系统 &copy; 2026</span>
      </el-footer>
    </el-container>


  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, ArrowDown, SwitchButton, VideoPlay, VideoPause, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const currentIndex = ref(-1)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const sliderValue = ref(0)
const isLoading = ref(false)
const errorMsg = ref('')

const audio = new Audio()
audio.preload = 'auto'

audio.addEventListener('timeupdate', () => {
  currentTime.value = audio.currentTime
  sliderValue.value = audio.currentTime
})

audio.addEventListener('loadedmetadata', () => {
  duration.value = audio.duration
  isLoading.value = false
  errorMsg.value = ''
})

audio.addEventListener('ended', () => {
  isPlaying.value = false
  if (currentIndex.value < songs.value.length - 1) {
    selectSong(currentIndex.value + 1)
  }
})

audio.addEventListener('error', () => {
  isLoading.value = false
  const err = audio.error
  if (err && err.code === 4) {
    errorMsg.value = '音频文件不存在或无法访问'
  } else {
    errorMsg.value = `音频加载失败 (${err?.code || '未知错误'})`
  }
})

const username = computed(() => localStorage.getItem('username') || '用户')
const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')

const currentSong = computed(() => currentIndex.value >= 0 ? songs.value[currentIndex.value] : null)

const songs = ref([
  { name: '七里香', artist: '周杰伦', file: 'qi_li_xiang.mp3' },
  { name: '晴天', artist: '周杰伦', file: 'qing_tian.mp3' },
  { name: '花海', artist: '周杰伦', file: 'hua_hai.mp3' },
  // { name: '春雪', artist: '音频怪物', file: 'chunxue.mp3' },
  { name: '白噪音', artist: '自然之声', file: 'baizaoyin.mp3' },
  { name: '雨声', artist: '自然之声', file: 'rain_sound.mp3' },
  { name: '拼个世界给自己', artist: '迷心', file: 'pin_ge_shi_jie_gei_zi_ji.mp3' },
  { name: '所以呢', artist: '迷心', file: 'suo_yi_ne.mp3' },
  { name: '去北极忘记你', artist: '迷心', file: 'qu_bei_ji_wang_ji_ni.mp3' },
  { name: '遇见你之前的我', artist: '迷心', file: 'yu_jian_ni_zhi_qian_de_wo.mp3' },
  { name: '冥想背景', artist: '冥想音乐', file: 'meditation_bg.mp3' },
  { name: '钢琴曲', artist: '钢琴纯音乐', file: 'piano_music.mp3' }
])

function selectSong(idx) {
  if (currentIndex.value === idx) {
    togglePlay()
    return
  }
  currentIndex.value = idx
  isLoading.value = true
  errorMsg.value = ''
  audio.src = `/music/${songs.value[idx].file}`
  audio.play().then(() => {
    isPlaying.value = true
    isLoading.value = false
  }).catch((e) => {
    isLoading.value = false
    if (e.name === 'NotAllowedError') {
      errorMsg.value = '浏览器阻止了自动播放，请点击播放按钮'
    } else if (e.name === 'NotSupportedError') {
      errorMsg.value = '该音频格式不受支持'
    } else {
      errorMsg.value = '播放失败: ' + e.message
    }
  })
}

function togglePlay() {
  if (!audio.src) return
  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
  } else {
    audio.play().then(() => {
      isPlaying.value = true
    }).catch((e) => {
      if (e.name === 'NotAllowedError') {
        errorMsg.value = '浏览器阻止了自动播放'
      }
    })
  }
}

function playPrev() {
  if (currentIndex.value > 0) selectSong(currentIndex.value - 1)
}

function playNext() {
  if (currentIndex.value < songs.value.length - 1) selectSong(currentIndex.value + 1)
}

function seekTo(val) {
  audio.currentTime = val
  currentTime.value = val
}

function formatTime(seconds) {
  if (!seconds || isNaN(seconds)) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
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

.player-header { margin-bottom: 24px; }
.player-header h2 { margin: 0; }
.player-subtitle { color: #999; font-size: 14px; margin-top: 4px; }

.player-card {
  display: flex;
  gap: 24px;
  align-items: center;
  padding: 30px;
  background: linear-gradient(135deg, #fdf2f8 0%, #fce4ec 100%);
  border-radius: 16px;
  border: 1px solid #f8bbd0;
}
.player-cover {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f48fb1, #d6336c);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s;
}
.player-cover.playing { animation: spin 4s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.cover-icon { font-size: 48px; color: white; }

.player-info { flex: 1; min-width: 0; }
.player-info h3 { margin: 0 0 4px; font-size: 20px; color: #d6336c; }
.player-info p { margin: 0 0 16px; color: #999; font-size: 14px; }

.player-controls { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }

.progress-bar { display: flex; align-items: center; gap: 8px; }
.progress-bar .time { font-size: 12px; color: #999; white-space: nowrap; }
.progress-bar :deep(.el-slider) { flex: 1; }
.progress-bar :deep(.el-slider__runway) { margin: 0; }

.empty-player { padding: 60px 0; }

.playlist { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.playlist-title { margin: 0 0 12px; font-size: 16px; color: #d6336c; }

.song-list { max-height: 420px; overflow-y: auto; }
.song-list::-webkit-scrollbar { width: 4px; }
.song-list::-webkit-scrollbar-thumb { background: #f8bbd0; border-radius: 2px; }

.song-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}
.song-item:hover { background: #fdf2f8; }
.song-item.active { background: #fce4ec; }
.song-idx { width: 24px; text-align: center; color: #d6336c; font-weight: 600; font-size: 14px; }
.song-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.song-name { font-size: 14px; color: #333; }
.song-artist { font-size: 12px; color: #999; }
.playing-indicator { color: #d6336c; }
</style>
