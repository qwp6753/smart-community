import request from './request'

export const listRecords = (params) => request.get('/records', { params })
