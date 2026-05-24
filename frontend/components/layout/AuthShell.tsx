import { GalleryVerticalEnd } from 'lucide-react';
import { FieldDescription } from '../ui/base/field';

export function AuthShell({ children }: { children: React.ReactNode }) {
    return (
        <div className="flex flex-col items-center justify-center gap-6">
            <div className="flex w-full min-w-xs max-w-sm flex-col gap-6">
                <a href="#" className="flex flex-col items-center gap-2 font-medium">
                    <div className="flex size-8 items-center justify-center rounded-md">
                        <GalleryVerticalEnd className="size-6" />
                    </div>
                    <span className="sr-only">Acme Inc.</span>
                    <h1 className="text-center text-xl font-bold">Welcome to Nova University</h1>
                </a>
                {children}
                <FieldDescription className="px-6 text-center">
                    By clicking continue, you agree to our <a href="#">Terms of Service</a> and
                    <a href="#">Privacy Policy</a>.
                </FieldDescription>
            </div>
        </div>
    );
}
