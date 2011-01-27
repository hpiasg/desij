.model test
.inputs c1_activate_rq
.dummy dum28 dum29 dum30
.graph
c1_activate_rq+ node9
dum28 dum29 node9
dum29 dum30
dum30 dum28
node9 dum29
p0 c1_activate_rq+
.marking {<dum28,dum29> p0}
.end