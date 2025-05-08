<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import LoginRegister from './components/LoginRegister.vue';

// 用户认证状态
const isAuthenticated = ref(false);
const username = ref('');
const showAuthForm = ref(false);

// 定义密码生成请求参数
const passwordForm = ref({
  length: 12,
  useUpper: true,
  useDigits: true,
  useSpecial: false
});

// 生成的密码
const generatedPassword = ref('');

// 密码强度评估
const passwordStrength = ref({
  score: 0,
  strength: '',
  feedback: ''
});

// 密码历史记录
const passwordHistory = ref([]);

// 显示历史记录
const showHistory = ref(false);

// 生成密码
async function generatePassword() {
  try {
    const headers = getAuthHeaders();
    const response = await axios.post('http://localhost:8080/api/generate', passwordForm.value, { headers });
    generatedPassword.value = response.data.password;
    ElMessage.success('密码生成成功！');
    // 生成后自动评估密码强度
    evaluatePasswordStrength(response.data.password);
    // 生成后自动获取新的历史记录
    if (showHistory.value) {
      fetchPasswordHistory();
    }
  } catch (error) {
    ElMessage.error('生成密码失败，请稍后再试！');
    console.error('生成密码出错:', error);
  }
}

// 获取历史记录
async function fetchPasswordHistory() {
  try {
    const headers = getAuthHeaders();
    const response = await axios.get('http://localhost:8080/api/history', { headers });
    passwordHistory.value = response.data;
  } catch (error) {
    console.error('获取历史记录出错:', error);
  }
}

// 复制密码到剪贴板
function copyPassword() {
  if (!generatedPassword.value) {
    ElMessage.warning('请先生成密码！');
    return;
  }
  
  navigator.clipboard.writeText(generatedPassword.value)
    .then(() => {
      ElMessage.success('密码已复制到剪贴板！');
    })
    .catch(err => {
      ElMessage.error('复制失败！');
      console.error('复制出错:', err);
    });
}

// 切换显示历史记录
function toggleHistory() {
  showHistory.value = !showHistory.value;
  if (showHistory.value) {
    fetchPasswordHistory();
  }
}

// 评估密码强度
async function evaluatePasswordStrength(password) {
  if (!password) return;
  
  try {
    const headers = getAuthHeaders();
    const response = await axios.post('http://localhost:8080/api/evaluate', { password }, { headers });
    passwordStrength.value = response.data;
  } catch (error) {
    console.error('评估密码强度出错:', error);
  }
}

// 获取密码强度对应的类型
function getStrengthType(strength) {
  switch(strength) {
    case '弱': return 'danger';
    case '中等': return 'warning';
    case '强': return 'success';
    case '非常强': return 'success';
    default: return 'info';
  }
}

// 检查用户是否已登录
function checkAuthentication() {
  const token = localStorage.getItem('token');
  const storedUsername = localStorage.getItem('username');
  
  if (token && storedUsername) {
    isAuthenticated.value = true;
    username.value = storedUsername;
    return true;
  }
  
  return false;
}

// 获取认证头信息
function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

// 处理登录成功
function handleLoginSuccess(loggedInUsername) {
  isAuthenticated.value = true;
  username.value = loggedInUsername;
  showAuthForm.value = false;
  
  // 登录后刷新历史记录
  if (showHistory.value) {
    fetchPasswordHistory();
  }
}

// 退出登录
function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  isAuthenticated.value = false;
  username.value = '';
  
  // 清空历史记录（因为退出后可能会显示公共记录）
  passwordHistory.value = [];
  
  // 如果正在显示历史记录，刷新为公共历史
  if (showHistory.value) {
    fetchPasswordHistory();
  }
  
  ElMessage.success('已退出登录');
}

// 页面加载时检查登录状态
onMounted(() => {
  checkAuthentication();
})
</script>

<template>
  <div class="background">
    <div class="container">
      <el-card class="password-generator">
        <template #header>
          <div class="header-title">
            <h1>随机密码生成器</h1>
            <div class="auth-controls">
              <template v-if="isAuthenticated">
                <el-dropdown trigger="click">
                  <span class="user-dropdown">
                    {{ username }} <el-icon><ArrowDown /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template v-else>
                <el-button type="primary" size="small" @click="showAuthForm = true">登录/注册</el-button>
              </template>
            </div>
          </div>
        </template>
        
        <!-- 用户登录/注册对话框 -->
        <el-dialog
          v-model="showAuthForm"
          title="用户认证"
          width="400px"
          destroy-on-close
        >
          <LoginRegister @login-success="handleLoginSuccess" />
        </el-dialog>
      
      <el-form :model="passwordForm" label-position="top">
        <el-form-item label="密码长度">
          <el-slider
            v-model="passwordForm.length"
            :min="8"
            :max="32"
            :step="1"
            show-input
            show-stops
          />
        </el-form-item>
        
        <el-form-item label="密码选项">
          <el-checkbox v-model="passwordForm.useUpper">包含大写字母</el-checkbox>
          <el-checkbox v-model="passwordForm.useDigits">包含数字</el-checkbox>
          <el-checkbox v-model="passwordForm.useSpecial">包含特殊字符</el-checkbox>
        </el-form-item>
        
        <el-button type="primary" @click="generatePassword" class="generate-btn">
          生成密码
        </el-button>
      </el-form>
      
      <div v-if="generatedPassword" class="password-result">
        <h3>生成结果：</h3>
        <el-input
          v-model="generatedPassword"
          readonly
          class="password-input"
        >
          <template #append>
            <el-button @click="copyPassword" type="primary">
              复制
            </el-button>
          </template>
        </el-input>
        
        <!-- 密码强度评估 -->
        <div v-if="passwordStrength.strength" class="strength-evaluation">
          <div class="strength-header">
            <h4>密码强度评估：</h4>
            <el-tag :type="getStrengthType(passwordStrength.strength)" effect="dark">
              {{ passwordStrength.strength }} ({{ passwordStrength.score }}/100)
            </el-tag>
          </div>
          
          <el-progress 
            :percentage="passwordStrength.score" 
            :status="getStrengthType(passwordStrength.strength)"
            :stroke-width="10"
            class="strength-progress"
          />
          
          <div class="strength-feedback">
            <el-alert
              :title="passwordStrength.feedback"
              :type="getStrengthType(passwordStrength.strength)"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </div>
      
      <div class="history-section">
        <el-divider>
          <el-button type="text" @click="toggleHistory">
            {{ showHistory ? '隐藏历史记录' : '显示历史记录' }}
          </el-button>
        </el-divider>
        
        <div v-if="!isAuthenticated && !showHistory" class="auth-prompt">
          <el-alert
            title="登录后可以保存和管理您的密码历史记录"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <el-button type="primary" size="small" @click="showAuthForm = true" class="auth-prompt-button">
                登录/注册
              </el-button>
            </template>
          </el-alert>
        </div>
        
        <div v-if="showHistory">
          <h3>历史记录</h3>
          <el-table :data="passwordHistory" style="width: 100%">
            <el-table-column prop="password" label="密码" />
            <el-table-column prop="length" label="长度" width="80" />
            <el-table-column label="选项" width="180">
              <template #default="scope">
                <el-tag v-if="scope.row.useUpper" size="small" type="success" class="option-tag">大写</el-tag>
                <el-tag v-if="scope.row.useDigits" size="small" type="info" class="option-tag">数字</el-tag>
                <el-tag v-if="scope.row.useSpecial" size="small" type="warning" class="option-tag">特殊字符</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="生成时间" width="180">
              <template #default="scope">
                {{ new Date(scope.row.createdAt).toLocaleString() }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>
    </div>
  </div>
</template>

<style scoped>
.background {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  background-color: #f0f2f5;
  overflow: auto;
}

.container {
  width: 100%;
  min-height: 100vh;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

.password-generator {
  width: 90%;
  max-width: none;
  margin: 20px 0;
}

.header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auth-controls {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.auth-prompt {
  margin-bottom: 20px;
}

.auth-prompt-button {
  margin-left: 15px;
}

.generate-btn {
  width: 100%;
  margin-top: 20px;
  margin-bottom: 20px;
}

.password-result {
  margin-top: 30px;
}

.password-input {
  font-family: monospace;
  font-size: 1.2em;
}

.strength-evaluation {
  margin-top: 15px;
  padding: 10px;
  border-radius: 5px;
  background-color: #f8f9fa;
}

.strength-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.strength-progress {
  margin: 15px 0;
}

.strength-feedback {
  margin-top: 10px;
}

.history-section {
  margin-top: 30px;
}

.option-tag {
  margin-right: 5px;
}

/* 用户登录相关样式 */
.el-dropdown-menu__item {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
