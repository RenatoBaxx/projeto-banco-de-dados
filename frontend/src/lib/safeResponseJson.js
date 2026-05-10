/**
 * Lê o corpo como texto e faz parse JSON; evita crash quando o proxy devolve HTML ou texto vazio.
 * @param {Response} res
 * @returns {{ data: object, parseFailed: boolean }}
 */
export async function safeResponseJson(res) {
  const text = await res.text();
  if (!text || !text.trim()) {
    return { data: {}, parseFailed: false };
  }
  try {
    return { data: JSON.parse(text), parseFailed: false };
  } catch {
    return { data: {}, parseFailed: true };
  }
}
