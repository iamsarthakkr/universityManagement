export function normalizeCodeSuffix(input: string, departmentCode: string) {
    const code = input.replace(/\s+/g, '').toUpperCase();
    return code.startsWith(departmentCode) ? code.slice(departmentCode.length) : code;
}
