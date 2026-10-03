const TOKEN_KEY = 'accessToken';

export function getToken(): string | null {
    try {
        return localStorage.getItem(TOKEN_KEY);
    } catch (error) {
        console.error('Unable to read access token from storage', error);
        return null;
    }
}

export function setToken(token: string): boolean {
    try {
        localStorage.setItem(TOKEN_KEY, token);
        return true;
    } catch (error) {
        console.error('Unable to save access token to storage', error);
        return false;
    }
}

export function clearToken(): boolean {
    try {
        localStorage.removeItem(TOKEN_KEY);
        return true;
    } catch (error) {
        console.error('Unable to remove access token from storage', error);
        return false;
    }
}
