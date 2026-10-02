import { Button } from '@/components/ui/base/button';
import { Card, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/base/card';
import { Spinner } from '@/components/ui/base/spinner';
import { Callback } from '@/types/common';

function Overlay({ children }: { children: React.ReactNode }) {
    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
            {children}
        </div>
    );
}

export function LoadingOverlay() {
    return (
        <Overlay>
            <div className="flex flex-col items-center gap-3 text-muted-foreground">
                <Spinner className="size-8" />
                <p className="text-sm">Loading...</p>
            </div>
        </Overlay>
    );
}

export function ErrorOverlay({ message, onRetry }: { message: string; onRetry: Callback }) {
    return (
        <Overlay>
            <Card className="w-full max-w-sm text-center">
                <CardHeader>
                    <CardTitle>Something went wrong</CardTitle>
                    <CardDescription>{message}</CardDescription>
                </CardHeader>
                <CardFooter className="justify-center">
                    <Button onClick={onRetry}>Try again</Button>
                </CardFooter>
            </Card>
        </Overlay>
    );
}
