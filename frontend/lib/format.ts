type Named = {
    firstName: string;
    lastName?: string | null;
};

export function fullName({ firstName, lastName }: Named) {
    return lastName ? `${firstName} ${lastName}` : firstName;
}
