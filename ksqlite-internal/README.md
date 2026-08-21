# Ksqlite Internal

Internal logic shared between modules of this project, split across two modules. Neither is
meant for public use, regardless of how it ends up wired into the dependency graph as more
modules potentially join it.

## [`runtime`](runtime)

Internal code shared between whichever modules need it.

## [`test`](test)

Test-only utilities shared by whichever modules need it.
