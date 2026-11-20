/**
 * 文件上传API
 */
import request from '@/utils/request'

/**
 * 上传图片
 */
export const uploadImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除图片
 */
export const deleteImage = (filename: string) => {
  return request({
    url: `/file/image/${filename}`,
    method: 'delete'
  })
}
