<template>
  <div class="page-container">
    <div class="page-header">
      <h2>订单管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">创建预订</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="订单号/客人姓名/手机" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="loadData" />
      <el-select v-model="orderStatusFilter" placeholder="全部订单状态" style="width: 160px" clearable @change="loadData">
        <el-option v-for="(label, key) in orderStatusMap" :key="key" :label="label" :value="Number(key)" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="guestName" label="客人姓名" width="100" />
      <el-table-column prop="guestPhone" label="联系手机" width="130" />
      <el-table-column label="客房" width="160">
        <template #default="{ row }">
          房号: {{ getRoomNumber(row.roomId) }} | {{ getRoomTypeName(row.roomTypeId) }}
        </template>
      </el-table-column>
      <el-table-column label="入住时间" width="230">
        <template #default="{ row }">
          {{ row.checkInDate }} ~ {{ row.checkOutDate }} ({{ row.days }}天)
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总价(元)" width="110" />
      <el-table-column label="支付状态" width="100">
        <template #default="{ row }">
          <el-tag :type="payStatusTagType[row.payStatus]">{{ payStatusMap[row.payStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="100">
        <template #default="{ row }">
          <el-tag :type="orderStatusTagType[row.orderStatus]">{{ orderStatusMap[row.orderStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="viewDetail(row)">详情</el-button>
          <el-dropdown>
            <el-button link type="primary" size="small">状态流转<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="(label, key) in orderStatusMap" :key="key" @click="updateOrderStatus(row, Number(key))">
                  {{ label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown>
            <el-button link type="warning" size="small">支付状态<el-icon><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="(label, key) in payStatusMap" :key="key" @click="updatePayStatus(row, Number(key))">
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
      style="margin-top: 16px; justify-content: flex-end; display: flex"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
    />

    <!-- 创建预订弹窗 -->
    <el-dialog v-model="createDialogVisible" title="创建预订订单" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="110px">
        <el-form-item label="客人姓名" prop="guestName"><el-input v-model="createForm.guestName" /></el-form-item>
        <el-form-item label="联系手机" prop="guestPhone"><el-input v-model="createForm.guestPhone" /></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="createForm.guestIdCard" /></el-form-item>
        <el-form-item label="客房" prop="roomId">
          <el-select v-model="createForm.roomId" style="width: 100%" filterable placeholder="选择空闲房间">
            <el-option v-for="r in availableRooms" :key="r.id" :label="`${r.roomNumber} (${getRoomTypeName(r.roomTypeId)})`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker v-model="createForm.checkInDate" type="date" placeholder="入住日期" value-format="YYYY-MM-DD" :disabled-date="disablePast" style="width: 100%" />
        </el-form-item>
        <el-form-item label="离店日期" prop="checkOutDate">
          <el-date-picker v-model="createForm.checkOutDate" type="date" placeholder="离店日期" value-format="YYYY-MM-DD" :disabled-date="disableCheckout" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="createForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">确认预订</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="500px">
      <el-descriptions v-if="currentOrder" :column="1" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客人姓名">{{ currentOrder.guestName }}</el-descriptions-item>
        <el-descriptions-item label="联系手机">{{ currentOrder.guestPhone }}</el-descriptions-item>
        <el-descriptions-item label="客房">房号:{{ getRoomNumber(currentOrder.roomId) }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ getRoomTypeName(currentOrder.roomTypeId) }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ currentOrder.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="离店日期">{{ currentOrder.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="入住天数">{{ currentOrder.days }}天</el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ currentOrder.price }}/天</el-descriptions-item>
        <el-descriptions-item label="总额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ payStatusMap[currentOrder.payStatus] }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ orderStatusMap[currentOrder.orderStatus] }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import request from '@/utils/request'

const list = ref([])
const availableRooms = ref([])
const roomTypes = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const orderStatusFilter = ref(null)
const loading = ref(false)
const createDialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

const orderStatusMap = { 0: '待确认', 1: '已确认', 2: '已入住', 3: '已退房', 4: '已取消' }
const payStatusMap = { 0: '未支付', 1: '已支付', 2: '已退款' }
const orderStatusTagType = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
const payStatusTagType = { 0: 'info', 1: 'success', 2: 'warning' }

const createForm = ref({
  guestName: '', guestPhone: '', guestIdCard: '', roomId: null,
  checkInDate: null, checkOutDate: null, remark: ''
})
const createRules = {
  guestName: [{ required: true, message: '请输入客人姓名', trigger: 'blur' }],
  guestPhone: [{ required: true, message: '请输入联系手机', trigger: 'blur' }],
  roomId: [{ required: true, message: '请选择客房', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }],
}

const roomNumberMap = ref({})
const roomTypeNameMap = ref({})

const getRoomTypeName = (id) => roomTypeNameMap.value[id] || '-'
const getRoomNumber = (id) => roomNumberMap.value[id] || '-'

const disablePast = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}
const disableCheckout = (date) => {
  if (!createForm.value.checkInDate) return false
  const checkIn = new Date(createForm.value.checkInDate)
  return date.getTime() <= checkIn.getTime()
}

const loadBaseData = async () => {
  const [roomsRes, typesRes] = await Promise.all([
    request.get('/room/with-type'),
    request.get('/room-type/list'),
  ])
  roomNumberMap.value = {}
  roomsRes.data.forEach(r => {
    roomNumberMap.value[r.id] = r.room_number
  })
  roomTypes.value = typesRes.data
  roomTypes.value.forEach(t => { roomTypeNameMap.value[t.id] = t.name })

  const availRes = await request.get('/room/available')
  availableRooms.value = availRes.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/order/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value, orderStatus: orderStatusFilter.value }
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const reset = () => { keyword.value = ''; orderStatusFilter.value = null; pageNum.value = 1; loadData() }

const openCreateDialog = async () => {
  createForm.value = { guestName: '', guestPhone: '', guestIdCard: '', roomId: null, checkInDate: null, checkOutDate: null, remark: '' }
  await loadBaseData()
  createDialogVisible.value = true
}

const handleCreate = async () => {
  await createFormRef.value.validate()
  creating.value = true
  try {
    const res = await request.post('/order', createForm.value)
    ElMessage.success(`预订成功!订单号: ${res.data.orderNo}`)
    createDialogVisible.value = false
    loadData()
  } finally {
    creating.value = false
  }
}

const viewDetail = async (row) => {
  const res = await request.get(`/order/${row.id}`)
  currentOrder.value = res.data
  detailDialogVisible.value = true
}

const updateOrderStatus = async (row, status) => {
  await request.put(`/order/${row.id}/status`, null, { params: { status } })
  ElMessage.success('状态更新成功')
  loadData()
}

const updatePayStatus = async (row, payStatus) => {
  await request.put(`/order/${row.id}/pay`, null, { params: { payStatus } })
  ElMessage.success('支付状态更新成功')
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除订单"${row.orderNo}"吗?`, '提示', { type: 'warning' })
  await request.delete(`/order/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(async () => {
  await loadBaseData()
  loadData()
})
</script>
