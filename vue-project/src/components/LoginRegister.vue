<template>
  <div class="auth-container">
    <el-tabs v-model="activeTab" class="auth-tabs">
      <el-tab-pane label="登录" name="login">
        <el-form :model="loginForm" ref="loginFormRef" :rules="loginRules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleLogin" class="auth-button" :loading="loading">
              登录
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      
      <el-tab-pane label="注册" name="register">
        <el-form :model="registerForm" ref="registerFormRef" :rules="registerRules" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleRegister" class="auth-button" :loading="loading">
              注册
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const emit = defineEmits(['login-success']);

const activeTab = ref('login');
const loading = ref(false);

// 登录表单
const loginFormRef = ref(null);
const loginForm = reactive({
  username: '',
  password: ''
});

// 注册表单
const registerFormRef = ref(null);
const registerForm = reactive({
  username: '',
  email: '',
  password: ''
});

// 登录验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

// 注册验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ]
};

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      
      try {
        const response = await axios.post('http://localhost:8080/api/auth/login', loginForm);
        
        // 存储令牌到本地存储
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('username', response.data.username);
        
        ElMessage.success('登录成功！');
        
        // 通知父组件登录成功
        emit('login-success', response.data.username);
        
        // 重置表单
        loginFormRef.value.resetFields();
      } catch (error) {
        console.error('登录出错:', error);
        
        let errorMessage = '登录失败，请稍后再试';
        if (error.response && error.response.data && error.response.data.error) {
          errorMessage = error.response.data.error;
        }
        
        ElMessage.error(errorMessage);
      } finally {
        loading.value = false;
      }
    }
  });
};

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      
      try {
        await axios.post('http://localhost:8080/api/auth/register', registerForm);
        
        ElMessage.success('注册成功！请登录');
        
        // 重置表单
        registerFormRef.value.resetFields();
        
        // 切换到登录标签
        activeTab.value = 'login';
      } catch (error) {
        console.error('注册出错:', error);
        
        let errorMessage = '注册失败，请稍后再试';
        if (error.response && error.response.data && error.response.data.error) {
          errorMessage = error.response.data.error;
        }
        
        ElMessage.error(errorMessage);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.auth-container {
  width: 100%;
  margin: 0 auto;
}

.auth-tabs {
  width: 100%;
}

.auth-button {
  width: 100%;
  margin-top: 10px;
}
</style>
