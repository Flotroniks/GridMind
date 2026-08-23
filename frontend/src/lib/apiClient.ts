const BASE_URL = import.meta.env.VITE_API_BASE_URL

/** Turns a relative API path (e.g. an item's `imageUrl`) into a fetchable absolute URL. */
export function resolveMediaUrl(path: string | null | undefined): string | null {
  return path ? `${BASE_URL}${path}` : null
}

export interface FieldErrors {
  [field: string]: string
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: FieldErrors

  constructor(status: number, message: string, fieldErrors: FieldErrors = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

export async function fetchJson<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  })

  return handleJsonResponse<T>(response)
}

/**
 * For multipart/form-data uploads. Deliberately doesn't go through fetchJson: the browser
 * must set its own `Content-Type: multipart/form-data; boundary=...` header from the
 * FormData body, which a manually-set `application/json` header (fetchJson's default)
 * would break.
 */
export async function postMultipart<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    body: formData,
  })

  return handleJsonResponse<T>(response)
}

async function handleJsonResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new ApiError(
      response.status,
      body?.message ?? `Request failed with status ${response.status}`,
      body?.fieldErrors ?? {},
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}
