import { z } from 'zod'

export const uuidSchema = z.string().uuid()

export const pageRequestSchema = z.object({
  page: z.number().int().min(0).default(0),
  size: z.number().int().min(1).max(100).default(20),
})

export const nameSchema = z.string().min(1).max(200)
export const descriptionSchema = z.string().max(2000).optional()

export type PageRequest = z.infer<typeof pageRequestSchema>
