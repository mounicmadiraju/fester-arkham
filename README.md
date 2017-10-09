# fester-arkham

![fester](http://i.imgur.com/7mCWBTV.jpg)

Copyright © 2017 Mounic Madiraju

Apache License

What it is
==========

Fester is a metrics aggregator consisting of a Storm service which
reads from a Kafka queue and emits events through a topology which
looks like the below:

![dataflow](/resources/dataflow.png)

In Fester, the raw metrics are saved to Cassandra immediately. Kafka
ensures that all metrics are seen by fester and Storm allows for
processing each metric *at least once*.

The metric key is used as the fields mapping value, so that the bolts
which rollup or receive raw metrics can be scaled horizontally by
setting an increased worker count.

The rollup bolts collect events for their period, write an aggregated
value to a rollup table and then emit an aggregated event. This means
we can chain any number of aggregation types and aggregation periods
together.
