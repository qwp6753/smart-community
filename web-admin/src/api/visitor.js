import request from './request'

export const listVisitors = (params) => request.get('/visitors', { params })
export const createVisitor = (data) => request.post('/visitors', data)
export const updateVisitor = (id, data) => request.put(`/visitors/${id}`, data)
