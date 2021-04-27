### ClearControl - Real-Time and High-Performance Control Library ###

ClearControl philosophy is that it is possible to design control software in
Java for high-performance microscopes with a minimum of native code by
leveraging the best Java practices and libraries available. 

### How to build the project? ###

* Building ClearControl:

to build the project:

    ./gradlew build

To generate eclipse project files:

    ./gradlew eclipse

To clean-up modified eclipse project files:

    ./gradlew cleanEclipse

That's it, now you can import the ClearControl project in Eclipse. 


* Dependencies
Some dependencies could not be found online - they are added into the Libs folder for now...


### Author(s) ###

* loic.royer@czbiohub.org
* rhaase@mpi-cbg.de
* ahmetcan.solak@czbiohub.org

---
# Notes

ClearControl repo can be considered as a core features repo. By itself, it is not able 
to control a custom microscope and run acquisition. All device adapters, concurrency 
machinery and data architecture can be found within this repository. In the intended 
design, placing any light sheet only code piece into this repository avoided. 
