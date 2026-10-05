export function toLocalIsoDate(date: Date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}

const DATE_FORMAT = new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });

// Parses `YYYY-MM-DD` as a local calendar date; `new Date('2026-10-01')` would be UTC midnight and can show the
// previous day west of UTC.
export function formatIsoDate(isoDate: string) {
    const [year, month, day] = isoDate.split('-').map(Number);
    return DATE_FORMAT.format(new Date(year, month - 1, day));
}

export function formatIsoDateRange(start: string, end: string) {
    return `${formatIsoDate(start)} – ${formatIsoDate(end)}`;
}
