<template>
  <div class="page-container">
    <div class="page-header">
      <h2>客房管理</h2>
      <div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增客房</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索房间号" style="width: 200px" clearable :prefix-icon="Search" @keyup.enter="loadData" />
      <el-select v-model="roomTypeId" placeholder="全部房型" style="width: 160px" clearable @change="loadData">
        <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="全部状态" style="width: 140px" clearable @change="loadData">
        <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="list" stripe v-loading="loading">
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="roomNumber" label="房间号" width="100" />
      <el-table-column label="房型" width="140">
        <template #default="{ row }">
          {{ getRoomTypeName(row.roomTypeId) }}
        </template>
      </el-table-column>
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType[row.status]">{{ statusMap[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
          <el-dropdown v-if="row.status === 0 || row.status === 3 || row.status === 4">
            <el-button link type="primary" size="small">更新状态<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="(label, key) in statusMap" :key="key" @click="updateStatus(row, Number(key))">
                  {{ label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客房' : '新增客房'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="form.roomNumber" />
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="form.roomTypeId" style="width: 100%">
            <el-option v-for="t in roomTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-input-number v-model="form.floor" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { Plus, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { pageRooms, createRoom, updateRoom, updateRoomStatus, deleteRoom } from '@/api/room'
import { listRoomTypes } from '@/api/roomType'

const list = ref([])
const roomTypes = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const roomTypeId = ref(null)
const statusFilter = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)

const statusMap = { 0: '空闲', 1: '已预订', 2: '已入住', 3: '清洁中', 4: '维修中' }
const statusTagType = { 0: 'success', 1: 'warning', 2: 'danger', 3: 'info', 4: 'info' }

const form = ref({ id: null, roomNumber: '', roomTypeId: null, floor: null, status: 0, remark: '' })
const rules = {
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
}

const getRoomTypeName = (id) => roomTypes.value.find(t => t.id === id)?.name || '-'

const loadRoomTypes = async () => {
  const res = await listRoomTypes()
  roomTypes.value = res.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageRooms({
      pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value, roomTypeId: roomTypeId.value, status: statusFilter.value
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const reset = () => { keyword.value = ''; roomTypeId.value = null; statusFilter.value = null; pageNum.value = 1; loadData() }

const openDialog = (row) => {
  if (row) {
    form.value = { ...row }
  } else {
    form.value = { id: null, roomNumber: '', roomTypeId: null, floor: null, status: 0, remark: '' }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.value.id) {
      await updateRoom(form.value)
    } else {
      await createRoom(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const updateStatus = async (row, status) => {
  await updateRoomStatus(row.id, status)
  ElMessage.success('状态更新成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除客房"${row.roomNumber}"吗?`, '提示', { type: 'warning' })
  await deleteRoom(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  await loadRoomTypes()
  loadData()
})
</script>
