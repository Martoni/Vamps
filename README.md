# Vamps
Hoppla getz foila un processeur avec Chisel et Lucienne.

## Tests

To launch test with treadle do :
sbt 'test:runMain vamps.VampsMain --generate-vcd-output on'

With verilator, and generate verilog sources:
sbt 'test:runMain vamps.VampsMain --backend-name verilator'

## Libero MiV Creative Board 

To generate verilog for MiV Creative Board do
sbt 'test:runMain vamps.MiVampsMain --backend-name verilator'

To test it :
sbt 'test:runMain vamps.MiVampsMain --generate-vcd-output on'

