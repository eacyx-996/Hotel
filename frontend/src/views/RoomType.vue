<template>
  <div class="page-container">
    <div class="page-header">
      <h2>房型管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增房型</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索房型名称" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="loadData" />
      <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="name" label="房型名称" />
      <el-table-column prop="price" label="单价(元/天)" width="130" />
      <el-table-column prop="area" label="面积(m²)" width="110" />
      <el-table-column prop="capacity" label="容纳人数" width="100" />
      <el-table-column prop="facilities" label="设施" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end; display: flex"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
    />

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房型' : '新增房型'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="房型名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="单价(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="面积(m²)">
          <el-input-number v-model="form.area" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="设施">
          <el-input v-model="form.facilities" placeholder="用逗号分隔" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = ref({
  id: null, name: '', price: 0, area: null, capacity: 2, facilities: '', description: '', status: 1,
})
const rules = {
  name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/room-type/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value } })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const reset = () => { keyword.value = ''; pageNum.value = 1; loadData() }

const openDialog = (row) => {
  if (row) {
    form.value = { ...row }
  } else {
    form.value = { id: null, name: '', price: 0, area: null, capacity: 2, facilities: '', description: '', status: 1 }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await request.put('/room-type', form.value)
    } else {
      await request.post('/room-type', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除房型"${row.name}"吗?`, '提示', { type: 'warning' })
  await request.delete(`/room-type/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
