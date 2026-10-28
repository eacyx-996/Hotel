import request from '@/utils/request'

export const pageOrders = (params) => request.get('/order/page', { params })

export const getOrderDetail = (id) => request.get(`/order/${id}`)

export const createOrder = (data) => request.post('/order', data)

export const updateOrderStatus = (id, status) => request.put(`/order/${id}/status`, null, { params: { status } })

export const updateOrderPayStatus = (id, payStatus) => request.put(`/order/${id}/pay`, null, { params: { payStatus } })

export const deleteOrder = (id) => request.delete(`/order/${id}`)
