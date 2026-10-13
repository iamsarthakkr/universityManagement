import { useAppStore } from '@/stores/appStore';
import { FilterSelect } from './FilterSelect';

type Props = {
    value: number | undefined;
    onChange: (departmentId: number | undefined) => void;
};

export function DepartmentFilter({ value, onChange }: Props) {
    const departments = useAppStore((state) => state.staticData.departments);
    const options = departments.map((department) => ({ value: department.id, label: department.name }));

    return (
        <FilterSelect
            label="Department"
            allLabel="All departments"
            value={value}
            options={options}
            onChange={onChange}
        />
    );
}
