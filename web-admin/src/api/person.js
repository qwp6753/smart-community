import request from './request'

export const listPersons = (params) => request.get('/persons', { params })
export const getPerson = (id) => request.get(`/persons/${id}`)
export const createPerson = (data) => request.post('/persons', data)
export const updatePerson = (id, data) => request.put(`/persons/${id}`, data)
export const deletePerson = (id) => request.delete(`/persons/${id}`)
