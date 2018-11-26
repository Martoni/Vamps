# Vamps
Chisel et Lucienne processor

## Tests

To launch test with treadle do :
```bash
$ sbt 'test:runMain vamps.VampsMain --generate-vcd-output on'
```

With verilator, and generate verilog sources:
```bash
$ sbt 'test:runMain vamps.VampsMain --backend-name verilator'
```
