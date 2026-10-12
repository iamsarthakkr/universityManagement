import { NativeSelect } from '@/components/ui/base/nativeSelect';

export type FilterOption<T extends string | number> = {
    value: T;
    label: string;
};

type Props<T extends string | number> = {
    label: string;
    value: T | undefined;
    options: FilterOption<T>[];
    onChange: (value: T | undefined) => void;
    allLabel?: string;
    disabled?: boolean;
};

export function FilterSelect<T extends string | number>(props: Props<T>) {
    const { label, value, options, onChange, allLabel = 'All', disabled = false } = props;

    // Native selects only hand back strings, so the chosen option is looked up instead of parsed.
    const handleChange = (raw: string) => {
        onChange(options.find((option) => String(option.value) === raw)?.value);
    };

    return (
        <label className="flex flex-col gap-1 text-xs font-medium text-text-muted sm:w-52">
            {label}
            <NativeSelect
                value={value === undefined ? '' : String(value)}
                disabled={disabled}
                onChange={(event) => handleChange(event.target.value)}
                className="text-text"
            >
                <option value="">{allLabel}</option>
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </NativeSelect>
        </label>
    );
}
