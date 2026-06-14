import re
from pathlib import Path

from pglast import parse_plpgsql, parse_sql


paths = [
    Path("database/supabase_schema.sql"),
    *sorted(Path("database/migrations").glob("*.sql")),
    Path("database/seed_demo.sql"),
]

total_statements = 0
total_functions = 0
marker = "$$"
language_before_body = re.compile(
    r"(CREATE\s+OR\s+REPLACE\s+FUNCTION.*?"
    r"LANGUAGE\s+plpgsql.*?AS\s+"
    + re.escape(marker)
    + r".*?"
    + re.escape(marker)
    + r";)",
    re.IGNORECASE | re.DOTALL,
)
language_after_body = re.compile(
    r"(CREATE\s+OR\s+REPLACE\s+FUNCTION.*?"
    r"AS\s+"
    + re.escape(marker)
    + r".*?"
    + re.escape(marker)
    + r"\s+LANGUAGE\s+plpgsql.*?;)",
    re.IGNORECASE | re.DOTALL,
)

for path in paths:
    sql = path.read_text(encoding="utf-8")
    statements = parse_sql(sql)
    functions = [
        *language_before_body.findall(sql),
        *language_after_body.findall(sql),
    ]
    do_blocks = re.findall(
        r"DO\s+\$\$(.*?)\$\$\s*;",
        sql,
        re.IGNORECASE | re.DOTALL,
    )

    for function in functions:
        parse_plpgsql(function)

    for index, body in enumerate(do_blocks):
        parse_plpgsql(
            "CREATE OR REPLACE FUNCTION "
            f"pg_temp.__validate_do_{index}() RETURNS void "
            "LANGUAGE plpgsql AS $$"
            f"{body}"
            "$$;"
        )

    if path.parent.name == "migrations":
        created_policies = {
            (table, name)
            for name, table in re.findall(
                r'CREATE\s+POLICY\s+"([^"]+)"\s+ON\s+public\.([a-z_]+)',
                sql,
                re.IGNORECASE,
            )
        }
        dropped_policies = {
            (table, name)
            for name, table in re.findall(
                r'DROP\s+POLICY\s+IF\s+EXISTS\s+"([^"]+)"\s+ON\s+public\.([a-z_]+)',
                sql,
                re.IGNORECASE,
            )
        }
        missing_drops = sorted(created_policies - dropped_policies)
        if missing_drops:
            raise ValueError(
                f"{path} is not repeatable; missing DROP POLICY for "
                f"{missing_drops}"
            )

    total_statements += len(statements)
    total_functions += len(functions) + len(do_blocks)
    print(
        f"{path}: {len(statements)} PostgreSQL statements, "
        f"{len(functions)} PL/pgSQL functions, "
        f"{len(do_blocks)} DO blocks"
    )

print(
    f"Total: {total_statements} PostgreSQL statements and "
    f"{total_functions} PL/pgSQL functions"
)
