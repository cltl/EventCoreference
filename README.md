EventCoreference
================
version 3.0
Copyright: VU University Amsterdam
email: piek.vossen@vu.nl

DESCRIPTION
Compares descriptions of events within and across documents to decide if they refer to the same events.
It creates an event-coreference layer within NAF for within document coreference and it creates RDF-TRiG according
to the SEM and GRASP model for cross-document coreference.


SOURCE CODE:

https://github.com/cltl/EventCoreference

Installation:
1. git clone https://github.com/cltl/EventCoreference
2. cd EventCoreference
3. chmod +wrx install.sh
4. install.sh

Installation through apache-maven-2.2.1 on the basis of the pom.xml

REQUIREMENTS
EventCoreference is developed in Java 1.6 and can run on any platform that supports Java 1.6

LICENSE
    EventCoreference is free software: you can redistribute it and/or modify
    it under the terms of the The Apache License, Version 2.0:
        http://www.apache.org/licenses/LICENSE-2.0.txt.

    EventCoreference is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
