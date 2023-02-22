# Simultaneous MultiView Lightsheet Microscope #

<img height="300" alt="Zebrafish embryo imaged with a simultaneous multiview lightsheet microscope" src="https://user-images.githubusercontent.com/1870994/200694511-778bb4c6-633c-483f-8e78-47ba3fc8b425.png"> <img height="300" alt="Simultaneous multiview lightsheet microscope" src="https://user-images.githubusercontent.com/1870994/200697163-249cf883-636f-4fff-bc5c-066486317ff5.PNG">

This open access repository provides all information for building a 2 detection and 2 illumination simultaneous multiview microscope:
- [a complete parts list](../../wiki/Multiview-Lightsheet-Microscope-Parts-List)
- [CAD drawings](../../wiki/CAD-Drawings)
- [control software](../../wiki/Control-Software) 
- [hardware building guide](../../wiki/Hardware-Building-Guide)
- [software setup instructions](../../wiki/Software-Setup-Instructions)
- [image data processing pipeline](../../wiki/Image-Data-Processing-with-DEXP) with [DEXP](https://github.com/royerlab/dexp)

This design is original but heavily influenced by the SiMView concept by the [Keller Lab](https://www.janelia.org/lab/keller-lab), and in particular these two publications: 
- [Adaptive light-sheet microscopy for long-term, high-resolution imaging in living organisms.](https://doi.org/10.1038/nbt.3708) Loic A. Royer, William C. Lemon, Raghav K. Chhetri, Yinan Wan, Michael Coleman, Eugene Myers and Philipp J. Keller. Nature Biotechnology (2016)
- [Quantitative high-speed imaging of entire developing embryos with simultaneous multiview light-sheet microscopy. ](https://doi.org/10.1038/nmeth.2062) Raju Tomer 1, Khaled Khairy, Fernando Amat, Philipp J Keller. Nature Methods
. 2012 

## Description:

One of the primary challenges of light-sheet microscopy is that it is difficult to achieve high-resolution images of larger, more optically challenging specimens, such as entire embryos, due to mismatches between the illuminating light-sheet and the focal plane of the detection objective. The microscope addresses this challenge by integrating a computational method that optimizes spatial resolution across the specimen volume in real time.

The microscope has several advantages. First, it can adapt to spatiotemporal dynamics of genetically encoded fluorescent markers. Second, it can robustly optimize imaging performance during large-scale morphogenetic changes in living organisms. Third, it can recover cellular and sub-cellular structures in many regions that are not resolved by non-adaptive imaging. Fourth, it is capable of long-term adaptive imaging of entire developing zebrafish and Drosophila embryos, as well as adaptive whole-brain functional imaging in larval zebrafish.

In summary, the adaptive light-sheet microscope is a powerful tool for live imaging of biological specimens that offers excellent spatial and temporal resolution and facilitates long-term observation of biological processes under physiological conditions.

