# GS16AO64c

Final version of module to control General Standards 64 channel DAC card (model 16AO64c)

http://www.generalstandards.com/view-products2.php?BD_family=16ao64c


This module consists of two classes:

### GSBuffer
	- Uses coremem to allocate memory.
	- Provides methods for function generation.
	- Provides methods for proper data formatting specifically for the 16AO64c

### GSSequencer:
	- Uses JNA to communicate with the DAC card
	- Receives ArrayDeque of GSBuffers
	- sends this ArrayDeque to the outputs continuously until it is empty.
	- Constantly checks the threshold and applies it to use upto 75% of the DAC memory.

Communication with the DAC drivers is mediated via JNA.  The JNA interface code is built using JNAerator, which is included in this repo.

Other documentation:

http://www.generalstandards.com/user-manuals/pcie_16ao64c_man_052018.pdf

http://www.generalstandards.com/specs/pcie_16ao64c_spec_090217.pdf
