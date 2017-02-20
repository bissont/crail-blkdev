# Crail on shared volume block deviceCrail-blkdev is an extension of the crail project to enable itto run on shared volume block devices.## BuildingClone and build [`jaio`](https://www.github.com/zrlio/jaio) dependency.Clone and build the project using:```bashmvn -DskipTests install```Then copy the jar files crail-blkdev-1.0.jar and its dependencies from the`target`folder into`$CRAIL_HOME/jars/`.Alternatively you can also put these files in your custom classpath.## Configuration parametersThe current code accepts following parameters (shown with their defaut values):```crail.datanode.blkdev.datapath          /dev/blkdevcrail.datanode.blkdev.storagelimit      1073741824crail.datanode.blkdev.allocationsize    1073741824crail.datanode.blkdev.queuedepth        16```You can put these values in `$CRAIL_HOME/conf/crail-site.conf`.## Starting a crail-blkdev datanode To start a crail-blkdev datanode, start a datanodes as ```bash $CRAIL_HOME/bin/crail datanode -t com.ibm.crail.datanode.blkdev.BlkDevStorageTier```in order for a client to automatically pick up connection to a new datanode type, you have to add following class to your list of datanode types in the`$CRAIL_HOME/conf/crail-site.conf` file. An example of such entry is :```bashcrail.datanode.types  com.ibm.crail.datanode.rdma.RdmaDataNode,com.ibm.crail.datanode.blkdev.BlkDevStorageTier```Please note that, this is a comma separated list of datanode **types** which defines the priorty order as well in which the blocks from a datanode will be consumed by the namenode. ## Setting up automatic deploymentTo enable deployment via `$CRAIL_HOME/bin/start-crail.sh` use the following extension in the crail slave file (`$CRAIL_HOME/conf/slave`.): ```bashhostname1 -t com.ibm.crail.datanode.blkdev.BlkDevStorageTier...```Note: A crail-blkdev datanode does not serve data requests from clients, butonly registers the block device storage information to the namenode. Such thatclients can directly access shared volume block devices with the offset information providedby the namenode.## Alignment requirementsThe crail block device client depends on jaio which requires buffers to be aligned toblock size (512B). For any application using the crail block device client you needto set the aligned direct memory option of the JVM:```-Dsun.nio.PageAlignDirectMemory=true```For example, in spark you can set the property `spark.executor.extraJavaOptions` in`spark-defaults.conf`.## ContributionsPRs are always welcome. Please fork, and make necessary modifications you propose, and let us know.## ContactIf you have questions or suggestions, feel free to post at:https://groups.google.com/forum/#!forum/zrlio-usersor email: zrlio-users@googlegroups.com  