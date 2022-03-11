//......xx  JOB FB3,
//             &SYSUID,
//             CLASS=A,
//             MSGLEVEL=(1,1)
//ST010    EXEC PGM=BPXBATCH,REGION=0M
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//STDIN    DD *
//STDPARM  DD PATH='/< path to stdparm file >/stdparm',
//            PATHOPTS=ORDONLY
