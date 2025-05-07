<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 定义密码生成请求参数
const passwordForm = ref({
  length: 12,
  useUpper: true,
  useDigits: true,
  useSpecial: false
});

// 生成的密码
const generatedPassword = ref('');

// 密码历史记录
const passwordHistory = ref([]);

// 显示历史记录
const showHistory = ref(false);

// 生成密码
async function generatePassword() {
  try {
    const response = await axios.post('http://localhost:8080/api/generate', passwordForm.value);
    generatedPassword.value = response.data.password;
    ElMessage.success('密码生成成功！');
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
    const response = await axios.get('http://localhost:8080/api/history');
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
</script>

<template>
  <div class="container">
    <el-card class="password-generator">
      <template #header>
        <div class="header-title">
          <h1>随机密码生成器</h1>
        </div>
      </template>
      
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
      </div>
      
      <div class="history-section">
        <el-divider>
          <el-button type="text" @click="toggleHistory">
            {{ showHistory ? '隐藏历史记录' : '显示历史记录' }}
          </el-button>
        </el-divider>
        
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
</template>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.password-generator {
  margin-top: 30px;
}

.header-title {
  text-align: center;
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

.history-section {
  margin-top: 30px;
}

.option-tag {
  margin-right: 5px;
}
</style>
