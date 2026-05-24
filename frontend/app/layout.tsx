import type { Metadata } from 'next';
import './globals.css';
import { Inter } from 'next/font/google';
import { cn } from '@/lib/cn';
import { Providers } from '@/context/Providers';

const inter = Inter({ subsets: ['latin'], variable: '--font-sans' });

export const metadata: Metadata = {
    title: 'University Enrollment',
    description: 'University enrollment management dashboard',
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
        <html lang="en" className={cn('font-sans', inter.variable)}>
            <body>
                <Providers>{children}</Providers>
            </body>
        </html>
    );
}
