#!/usr/bin/env python3
"""
Generates a demo seed file for the server's DemoDataSeeder (format: seed/dto/SeedFile.java).

    python3 seed-tools/generate_seed.py --size dev  --out src/main/resources/seed/seed.example.json
    python3 seed-tools/generate_seed.py --size prod --out ../deploy/seed/seed.json

Everything except passwords is deterministic, so regenerating gives the same people, courses and
enrollments. dev uses the password "password" for every account (allowed only by the dev profile);
prod gives every account its own random password, written into the file, which is gitignored and is
the place to look one up.

All dates are offsets from the day the seed is applied ("-190d"), so the data never ages out of shape:
there is always a running semester with an open registration window, older completed ones, and a
planned one.
"""

import argparse
import json
import random
import secrets
import sys
from dataclasses import dataclass, field

FORMAT_VERSION = 2
DEV_PASSWORD = "password"

DEPARTMENTS = {
    "CSE": "Computer Science and Engineering",
    "MATH": "Mathematics",
    "PHYS": "Physics",
    "ECE": "Electronics and Communication Engineering",
}

COURSES = {
    "CSE": [
        ("CS101", "Introduction to Programming", "Variables, control flow, functions and basic data structures.", 4),
        ("CS201", "Data Structures", "Lists, trees, hash tables and graphs, with their costs.", 4),
        ("CS202", "Algorithms", "Sorting, searching, greedy methods, dynamic programming and graph algorithms.", 4),
        ("CS301", "Databases", "Relational modelling, SQL, transactions and indexing.", 3),
        ("CS302", "Operating Systems", "Processes, scheduling, memory management and file systems.", 3),
    ],
    "MATH": [
        ("MA101", "Calculus I", "Limits, derivatives and integrals of one variable.", 4),
        ("MA102", "Calculus II", "Series, multivariable calculus and differential equations.", 4),
        ("MA201", "Linear Algebra", "Vector spaces, matrices, eigenvalues.", 3),
        ("MA202", "Probability and Statistics", "Random variables, distributions, estimation and hypothesis tests.", 3),
        ("MA301", "Real Analysis", "Sequences, continuity and the rigorous foundations of calculus.", 3),
    ],
    "PHYS": [
        ("PH101", "Mechanics", "Newtonian mechanics, energy, momentum and rotation.", 4),
        ("PH102", "Electricity and Magnetism", "Electrostatics, circuits, magnetic fields and Maxwell's equations.", 4),
        ("PH201", "Quantum Mechanics", "Wave functions, the Schrodinger equation and simple quantum systems.", 3),
        ("PH202", "Thermodynamics", "Laws of thermodynamics, entropy and statistical ideas.", 3),
    ],
    "ECE": [
        ("EC101", "Circuit Theory", "Network theorems, transient and steady-state analysis.", 4),
        ("EC201", "Signals and Systems", "Continuous and discrete signals, Fourier and Laplace transforms.", 3),
        ("EC202", "Digital Electronics", "Logic gates, combinational and sequential circuits.", 3),
        ("EC301", "Communication Systems", "Modulation, noise and digital communication.", 3),
    ],
}

INSTRUCTORS = {
    "CSE": [("Ada", "Lovelace"), ("Alan", "Turing"), ("Edsger", "Dijkstra"), ("Barbara", "Liskov")],
    "MATH": [("Emmy", "Noether"), ("Srinivasa", "Ramanujan"), ("Sophie", "Germain"), ("Carl", "Gauss")],
    "PHYS": [("Marie", "Curie"), ("Chandrasekhara", "Raman"), ("Richard", "Feynman")],
    "ECE": [("Claude", "Shannon"), ("Jagadish", "Bose"), ("Hedy", "Lamarr"), ("Nikola", "Tesla")],
}

# The documented demo logins: a CSE student, and the first CSE instructor (Ada Lovelace)
DEMO_STUDENT = ("CSE", "Riya", "Sharma")
DEMO_STUDENT_USERNAME = "riya.sharma"

PENDING_INSTRUCTORS = [("CSE", "Grace", "Hopper"), ("PHYS", "Satyendra", "Bose"), ("MATH", "Katherine", "Johnson")]
REJECTED_INSTRUCTORS = [("CSE", "Charles", "Babbage"), ("ECE", "Guglielmo", "Marconi")]

FIRST_NAMES_F = ["Riya", "Sara", "Meera", "Ananya", "Diya", "Ishita", "Kavya", "Nisha", "Priya", "Aisha", "Tara",
                 "Zoya", "Pooja", "Sneha", "Neha", "Anika", "Myra", "Saanvi", "Emma", "Olivia", "Sofia", "Lena"]
FIRST_NAMES_M = ["Arjun", "Kabir", "Rohan", "Aarav", "Vihaan", "Aditya", "Ishaan", "Karan", "Rahul", "Siddharth",
                 "Yash", "Dev", "Nikhil", "Varun", "Aman", "Kunal", "Liam", "Noah", "Lucas", "Omar", "Mateo"]
LAST_NAMES = ["Sharma", "Mehta", "Khan", "Singh", "Iyer", "Patel", "Gupta", "Reddy", "Nair", "Das", "Joshi",
              "Kapoor", "Verma", "Rao", "Chopra", "Bose", "Malhotra", "Menon", "Pillai", "Banerjee", "Fernandes",
              "Kulkarni", "Agarwal", "Bhat", "Garcia", "Smith", "Ali", "Chen", "Kim", "Mukherjee"]
HOSTELS = ["Hostel A", "Hostel B", "Hostel C", "Hostel D"]


@dataclass(frozen=True)
class Size:
    departments: list
    courses_per_department: int
    instructors_per_department: int
    students_per_department: tuple   # (min, max)
    completed_semesters: int
    courses_per_student: tuple       # (min, max) per semester
    pending_students_per_department: int
    rejected_students_per_department: int
    pending_instructors: int
    rejected_instructors: int


SIZES = {
    "dev": Size(["CSE", "MATH"], 3, 2, (5, 5), 2, (1, 2), 1, 1, 1, 1),
    "prod": Size(["CSE", "MATH", "PHYS", "ECE"], 5, 4, (12, 18), 3, (2, 3), 2, 1, 3, 2),
}

# Semesters are 183 days apart with alternating terms, so two semesters of the same term start 366 days
# apart and can never share a year, whatever day the file is applied (unique term + year).
SEMESTER_SPACING = 183

@dataclass
class Semester:
    key: str
    term: str
    status: str
    start: int            # day offsets from the apply date
    registration_start: int
    registration_end: int
    end: int

    def to_json(self):
        return {
            "key": self.key, "term": self.term, "status": self.status,
            "registrationStartDate": offset(self.registration_start),
            "registrationEndDate": offset(self.registration_end),
            "startDate": offset(self.start), "endDate": offset(self.end),
        }


@dataclass
class Generator:
    size: Size
    rng: random.Random
    password_for: callable
    usernames: set = field(default_factory=set)
    phones_given: int = 0
    output: dict = field(default_factory=dict)

    def generate(self):
        departments = self.size.departments
        semesters = self.semesters()
        courses = {code: COURSES[code][:self.size.courses_per_department] for code in departments}
        instructors = {code: self.instructors(code, semesters) for code in departments}
        students = {code: self.students(code, semesters) for code in departments}

        offerings = []
        for index, semester in enumerate(semesters):
            for code in departments:
                teachers = instructors[code]
                for course_index, course in enumerate(courses[code]):
                    teacher = teachers[(course_index + index) % len(teachers)]
                    offerings.append({"key": offering_key(course[0], semester), "course": course[0],
                                      "semester": semester.key, "instructor": teacher["username"],
                                      "section": "A", "capacity": None, "_semester": semester, "_department": code})

        enrollments = self.enrollments(semesters, students, offerings)
        self.cover_every_status(enrollments, offerings)
        self.set_capacities(offerings, enrollments, semesters)

        self.output = {
            "formatVersion": FORMAT_VERSION,
            "departments": [{"code": code, "name": DEPARTMENTS[code]} for code in departments],
            "instructors": [i for code in departments for i in instructors[code]],
            "students": [s["json"] for code in departments for s in students[code]],
            "courses": [{"code": c[0], "department": code, "title": c[1], "description": c[2], "credits": c[3]}
                        for code in departments for c in courses[code]],
            "semesters": [s.to_json() for s in semesters],
            "courseOfferings": [{k: v for k, v in o.items() if not k.startswith("_")} for o in offerings],
            "enrollments": enrollments,
            "studentRegistrations": self.student_requests(semesters[-2]),
            "instructorRegistrations": self.instructor_requests(semesters[-2]),
        }
        return self.output

    def semesters(self):
        # index 0 is the running semester; it started a week ago and its registration window is open
        result = []
        for index in range(-self.size.completed_semesters, 2):
            start = -7 + SEMESTER_SPACING * index
            term = "WINTER" if index % 2 == 0 else "SUMMER"
            if index < 0:
                key, status = f"completed{index}", "COMPLETED"
            elif index == 0:
                key, status = "current", "ACTIVE"
            else:
                key, status = "next", "PLANNED"
            result.append(Semester(key, term, status, start, start - 8, start + 22, start + 127))
        return result

    def instructors(self, department, semesters):
        first = semesters[0]
        result = []
        for first_name, last_name in INSTRUCTORS[department][:self.size.instructors_per_department]:
            username = self.claim(first_name, last_name)
            requested = first.registration_start - self.rng.randint(30, 60)
            result.append({
                "username": username, "email": email(username), "password": self.password_for(username),
                "department": department, "firstName": first_name, "lastName": last_name,
                "phoneNumber": self.next_phone(),
                "registration": {"requestedAt": offset(requested), "reviewedAt": offset(requested + self.rng.randint(1, 4))},
            })
        return result

    def students(self, department, semesters):
        count = self.rng.randint(*self.size.students_per_department)
        last_completed = len(semesters) - 3
        result = []
        for number in range(count):
            # Most started in the oldest semester; some joined later, so histories differ in length
            joined = 0 if number < count * 0.6 else self.rng.randint(0, last_completed + 1)
            if department == DEMO_STUDENT[0] and number == 0:
                first_name, last_name = DEMO_STUDENT[1:]
                # One semester of history leaves plenty of their own courses open for the running one
                joined = last_completed
            else:
                first_name, last_name = self.person()
            username = self.claim(first_name, last_name)
            joined_semester = semesters[joined]
            requested = joined_semester.registration_start - self.rng.randint(10, 40)
            result.append({
                "joined": joined,
                "passed": set(),
                "json": {
                    "username": username, "email": email(username), "password": self.password_for(username),
                    "department": department, "firstName": first_name, "lastName": last_name,
                    "dateOfBirth": f"{self.rng.randint(2001, 2006)}-{self.rng.randint(1, 12):02d}-{self.rng.randint(1, 28):02d}",
                    "phoneNumber": self.next_phone(),
                    "address": f"{self.rng.choice(HOSTELS)}, Room {self.rng.randint(1, 240)}",
                    "fatherName": f"{self.rng.choice(FIRST_NAMES_M)} {last_name}",
                    "motherName": f"{self.rng.choice(FIRST_NAMES_F)} {last_name}",
                    "registration": {"requestedAt": offset(requested),
                                     "reviewedAt": offset(requested + self.rng.randint(1, 5))},
                },
            })
        return result

    def enrollments(self, semesters, students, offerings):
        by_semester = {}
        for o in offerings:
            by_semester.setdefault(o["_semester"].key, []).append(o)

        result = []
        for index, semester in enumerate(semesters):
            if semester.status == "PLANNED":
                continue   # its registration window hasn't opened, so nobody could have asked yet
            for code, people in students.items():
                for student in people:
                    if student["joined"] > index:
                        continue
                    demo = student["json"]["username"] == DEMO_STUDENT_USERNAME and semester.status == "ACTIVE"
                    picks = self.pick_courses(student, code, by_semester[semester.key], demo)
                    for position, offering in enumerate(picks):
                        status = self.enrollment_status(semester)
                        if demo:
                            # The demo student always has a seat and a request waiting for the demo instructor
                            status = "PENDING" if position == 0 else "ENROLLED"
                        if status == "ENROLLED":
                            student["passed"].add(offering["course"])
                        result.append({
                            "student": student["json"]["username"], "offering": offering["key"],
                            "status": status, "requestedAt": offset(self.request_day(semester)),
                        })
        return result

    def pick_courses(self, student, department, offerings, demo):
        # Nobody repeats a course they already finished; once their own department runs out, they take
        # courses from the others, as electives
        open_courses = [o for o in offerings if o["course"] not in student["passed"]]
        own = [o for o in open_courses if o["_department"] == department]
        other = [o for o in open_courses if o["_department"] != department]
        wanted = max(self.rng.randint(*self.size.courses_per_student), 2 if demo else 1)
        if demo:
            # Prefer a course the demo instructor teaches, so they have a request to review
            own.sort(key=lambda o: o["instructor"] != claim_name(*INSTRUCTORS[department][0]))
            picks = own[:1] + self.rng.sample(own[1:], max(0, min(wanted - 1, len(own) - 1)))
        else:
            picks = self.rng.sample(own, min(wanted, len(own)))
        if len(picks) < wanted or self.rng.random() < 0.25:
            picks += self.rng.sample(other, min(max(wanted - len(picks), 1), len(other)))
        return picks

    def cover_every_status(self, enrollments, offerings):
        # A small file can miss a status by chance; turn finished history rows into the missing ones
        # so every state shows up in the UI
        semester_of = {o["key"]: o["_semester"] for o in offerings}
        present = {e["status"] for e in enrollments}
        for status in ("REJECTED", "DROPPED", "CANCELLED"):
            if status in present:
                continue
            candidates = [e for e in enrollments if e["status"] == "ENROLLED"
                          and semester_of[e["offering"]].status == "COMPLETED"
                          and e["student"] != DEMO_STUDENT_USERNAME]
            self.rng.choice(candidates)["status"] = status

    def enrollment_status(self, semester):
        roll = self.rng.random()
        if semester.status == "COMPLETED":
            return "ENROLLED" if roll < 0.78 else "REJECTED" if roll < 0.88 else "DROPPED" if roll < 0.95 else "CANCELLED"
        return "ENROLLED" if roll < 0.55 else "PENDING" if roll < 0.88 else "REJECTED" if roll < 0.93 \
            else "CANCELLED" if roll < 0.97 else "DROPPED"

    def request_day(self, semester):
        # Inside the registration window, and never in the future for the running semester
        return self.rng.randint(semester.registration_start, min(semester.registration_end, 0))

    def set_capacities(self, offerings, enrollments, semesters):
        enrolled, requested = {}, {}
        for e in enrollments:
            requested[e["offering"]] = requested.get(e["offering"], 0) + 1
            if e["status"] == "ENROLLED":
                enrolled[e["offering"]] = enrolled.get(e["offering"], 0) + 1

        demo_pending = {e["offering"] for e in enrollments if e["student"] == DEMO_STUDENT_USERNAME and e["status"] == "PENDING"}
        current = [o for o in offerings if o["_semester"].status == "ACTIVE" and o["key"] not in demo_pending]
        # One running offering is full, to show the "offering is full" state; pick the busiest
        full = max(current, key=lambda o: (enrolled.get(o["key"], 0), o["key"]))["key"] if current else None
        for o in offerings:
            taken = enrolled.get(o["key"], 0)
            if o["key"] == full:
                o["capacity"] = max(taken, 1)
            else:
                o["capacity"] = max(20, -(-(requested.get(o["key"], 0) + 5) // 10) * 10)

    def student_requests(self, current):
        result = []
        for code in self.size.departments:
            for status, count in (("PENDING", self.size.pending_students_per_department),
                                  ("REJECTED", self.size.rejected_students_per_department)):
                for _ in range(count):
                    first_name, last_name = self.person()
                    username = self.claim(first_name, last_name)
                    result.append({
                        **self.request(status, current), "username": username, "email": email(username),
                        "password": self.password_for(username), "department": code,
                        "firstName": first_name, "lastName": last_name,
                        "dateOfBirth": f"{self.rng.randint(2005, 2007)}-{self.rng.randint(1, 12):02d}-{self.rng.randint(1, 28):02d}",
                    })
        return result

    def instructor_requests(self, current):
        result = []
        departments = set(self.size.departments)
        for status, people, count in (("PENDING", PENDING_INSTRUCTORS, self.size.pending_instructors),
                                      ("REJECTED", REJECTED_INSTRUCTORS, self.size.rejected_instructors)):
            for code, first_name, last_name in [p for p in people if p[0] in departments][:count]:
                username = self.claim(first_name, last_name)
                result.append({
                    **self.request(status, current), "username": username, "email": email(username),
                    "password": self.password_for(username), "department": code,
                    "firstName": first_name, "lastName": last_name,
                })
        return result

    def request(self, status, current):
        if status == "PENDING":
            return {"status": status, "requestedAt": offset(-self.rng.randint(0, 10))}
        requested = self.rng.randint(current.start - 120, current.start - 20)
        return {"status": status, "requestedAt": offset(requested),
                "reviewedAt": offset(requested + self.rng.randint(1, 6))}

    def person(self):
        first = self.rng.choice(FIRST_NAMES_F if self.rng.random() < 0.5 else FIRST_NAMES_M)
        return first, self.rng.choice(LAST_NAMES)

    # 000000xxxx can't be dialled anywhere, so a seeded number never reaches a real person
    def next_phone(self):
        self.phones_given += 1
        return f"{self.phones_given:010d}"

    def claim(self, first_name, last_name):
        base = claim_name(first_name, last_name)
        username, suffix = base, 2
        while username in self.usernames:
            username, suffix = f"{base}{suffix}", suffix + 1
        self.usernames.add(username)
        return username


def claim_name(first_name, last_name):
    return f"{first_name}.{last_name}".lower()


def offering_key(course_code, semester):
    return f"{course_code.lower()}-{semester.key}-a"


def offset(days):
    return f"{days:+d}d"


def email(username):
    return f"{username}@example.com"


def render(data):
    # One record per line: easy to read, and a diff shows exactly which records changed
    lines = ["{", f'  "formatVersion": {data["formatVersion"]},']
    sections = [k for k in data if k != "formatVersion"]
    for position, section in enumerate(sections):
        records = data[section]
        closing = "," if position < len(sections) - 1 else ""
        if not records:
            lines.append(f'  "{section}": []{closing}')
            continue
        lines.append(f'  "{section}": [')
        lines += [f"    {json.dumps(r, ensure_ascii=False)}{',' if i < len(records) - 1 else ''}"
                  for i, r in enumerate(records)]
        lines.append(f"  ]{closing}")
    lines.append("}")
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description="Generate a demo seed file.")
    parser.add_argument("--size", choices=SIZES, required=True)
    parser.add_argument("--out", help="file to write (default: stdout)")
    args = parser.parse_args()

    if args.size == "dev":
        password_for = lambda username: DEV_PASSWORD
    else:
        # Not from the seeded rng: the people are reproducible, their passwords must not be
        password_for = lambda username: secrets.token_urlsafe(12)

    data = Generator(SIZES[args.size], random.Random(2026), password_for).generate()
    text = render(data)
    if args.out:
        with open(args.out, "w", encoding="utf-8") as out:
            out.write(text)
    else:
        sys.stdout.write(text)

    counts = ", ".join(f"{k}={len(v)}" for k, v in data.items() if isinstance(v, list))
    print(f"{args.size}: {counts}", file=sys.stderr)


if __name__ == "__main__":
    main()
