import request from '@/utils/request'

// ========== 环境准备与装载 ==========

export function getEnvTemplates() {
  return request({
    url: '/rag-api/security/env-templates',
    method: 'get'
  })
}

export function getEnvStatus() {
  return request({
    url: '/rag-api/security/env-status',
    method: 'get'
  })
}

export function getEnvInstances() {
  return request({
    url: '/rag-api/security/env-instances',
    method: 'get'
  })
}

export function initEnvironment(data) {
  return request({
    url: '/rag-api/security/env-init',
    method: 'post',
    data
  })
}

export function testEnvironmentChat(data) {
  return request({
    url: '/rag-api/security/env-test-chat',
    method: 'post',
    data
  })
}

// ========== 攻击配置与执行 ==========

export function getAttackTasks() {
  return request({
    url: '/rag-api/security/attack-tasks',
    method: 'get'
  })
}

export function createAttackTask(data) {
  return request({
    url: '/rag-api/security/attack-task',
    method: 'post',
    data
  })
}

export function executeAttack(data) {
  return request({
    url: '/rag-api/security/attack-execute',
    method: 'post',
    data
  })
}

export function getAttackMethods() {
  return request({
    url: '/rag-api/security/attack-methods',
    method: 'get'
  })
}

// ========== 防御配置与执行 ==========

export function getDefenseResults() {
  return request({
    url: '/rag-api/security/defense-results',
    method: 'get'
  })
}

export function executeDefense(data) {
  return request({
    url: '/rag-api/security/defense-execute',
    method: 'post',
    data
  })
}

export function getSituation() {
  return request({
    url: '/rag-api/security/situation',
    method: 'get'
  })
}

// ========== 复盘分析与记录 ==========

export function getVerifyTasks() {
  return request({
    url: '/rag-api/security/verify-tasks',
    method: 'get'
  })
}

export function getEvaluationReports() {
  return request({
    url: '/rag-api/security/evaluation-reports',
    method: 'get'
  })
}

export function createEvaluationReport(data) {
  return request({
    url: '/rag-api/security/evaluation-report',
    method: 'post',
    data
  })
}
