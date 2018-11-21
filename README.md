# Vamps
Chisel et Lucienne processor

## Tests

To launch test with treadle do :
$ sbt 'test:runMain vamps.VampsMain --generate-vcd-output on'

With verilator, and generate verilog sources:
sbt 'test:runMain vamps.VampsMain --backend-name verilator'
