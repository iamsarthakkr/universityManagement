import { ComponentProps } from 'react';

import { NativeSelect } from '@/components/ui/base/nativeSelect';
import { useAppStore } from '@/stores/appStore';

type Props = Omit<ComponentProps<'select'>, 'value' | 'onChange'> & {
    value: number | null;
    onChange: (departmentId: number) => void;
};

export function DepartmentSelect({ value, onChange, ...props }: Props) {
    const departments = useAppStore((state) => state.staticData.departments);

    return (
        <NativeSelect value={value ?? ''} onChange={(event) => onChange(Number(event.target.value))} {...props}>
            <option value="" disabled>
                Select a department
            </option>
            {departments.map((department) => (
                <option key={department.id} value={department.id}>
                    {department.name}
                </option>
            ))}
        </NativeSelect>
    );
}
