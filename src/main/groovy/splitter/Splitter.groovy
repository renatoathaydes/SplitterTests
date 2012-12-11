package splitter

//
// Copyright 2011 SmartBear Software
//
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
//
// http://ec.europa.eu/idabc/eupl5
//
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
//

/**
 * Splits input to specified number of outputs
 *
 * @id com.eviware.Splitter
 * @help http://www.loadui.org/Flow-Control/splitter-component.html
 * @category flow
 * @nonBlocking true
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eviware.loadui.ui.fx.util.Properties
import javafx.scene.control.Slider
import javafx.beans.InvalidationListener

//Here to support Splitters created in loadUI 1.0, remove in the future:
try { renameProperty( 'outputs', 'numOutputs' ) } catch( e ) {}

incomingTerminal.description = 'Received messages will be outputted in different output terminals.'

random = new Random()

total = counters['total_output']
countDisplays = [:]
terminalProbabilities = [:]
userChanges = []
resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
propagatedIndexes = [:]
vals = [ 100, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
doAfterPropagations = []

for( i=0; i < outgoingTerminalList.size(); i++ ) {
	countDisplays[i] = { counters["output_$i"].get() - resetValues[i] }
	initialValue = outgoingTerminalList.size() > 1 ? 0 : 100
	createProbabilityProperty(i)
}

def createProbabilityProperty( i )
{
	terminalProbabilities[i] = createProperty( 'probability' + i, Integer, initialValue ) { newVal, oldVal ->

		log.info("***************Setting index $i, new: $newVal, old: $oldVal")

		if( oldVal != null  ) {
			onChangeValue(i, oldVal, ensureBounds(newVal))
			log.info("END, propagated: " + propagatedIndexes)
		}

	}

}

def onChangeValue( i, oldVal, newVal ) {
	log.info("Propagated: " + propagatedIndexes)
	if (propagatedIndexes.containsKey(i)  && propagatedIndexes.get( i ) == newVal) {
		log.info("Index propagated: $i, will not trigger handler")
		propagatedIndexes.remove( i )
		next();
	} else if ( !propagatedIndexes.isEmpty() ) {
		doAfterPropagations << [ i, oldVal, newVal ]
	} else {

		log.info("********** Requesting to change index " + i + " from $oldVal to $newVal, history : " + userChanges)
		vals[i] = newVal

		def forbidden = [ i ]
		def valsSum;
		while (( valsSum = sum( vals ) ) != 100) {
			compensateAfterChange( forbidden, valsSum );
		}

		// userChanges behaves as a sorted (insertion order) Set, so before adding anything to it, we try to remove it
		userChanges -= i;
		userChanges << i;

		next();

	}

}

def compensateAfterChange( forbiddenIndexes, valsSum ) {

	log.info("All Values = $vals sum to " + valsSum )
	def toChange = findIndexToChange( forbiddenIndexes, valsSum )
	log.info("Will change $toChange")
	forbiddenIndexes << toChange;
	
	def desiredValue = ensureBounds( vals[toChange] - (valsSum - 100) );
	vals[toChange] = desiredValue
	propagatedIndexes.put( toChange, desiredValue );
	
	log.info( "Values: " + vals );
	terminalProbabilities[ toChange ].value = desiredValue

}

def sum( vals ) {
	def sum = 0;
	vals.each { sum += it }
	return sum;
}

def ensureBounds( value ) {
	return Math.max( 0, Math.min( 100, value ) );
}

def findIndexToChange(forbiddenIndexes, valsSum) {
	def toChange = null;
	log.info("Trying to find index to change, forbidden: $forbiddenIndexes, sum: $valsSum")
	
	// try to find a terminal which has not been changed yet
	toChange = ( 0 ..< terminalProbabilities.size() ).find {
		!forbiddenIndexes.contains( it ) && !userChanges.contains( it ) &&
		( valsSum < 100 || ( valsSum > 100 && vals[it] > 0 ) )
	}
	
	// if not found above, try to find the knob changed the longest time ago which is not forbidden
	if (toChange == null) {
		toChange = userChanges.find {
			!forbiddenIndexes.contains( it ) &&
			( valsSum < 100 || ( valsSum > 100 && vals[it] > 0 ) )
		}
	} else {
		log.info("Found index first time")
	}
	
	
	// if still not found, find the first terminal which is not forbidden
	if (toChange == null) {
		toChange = ( 0..terminalProbabilities.size() ).find { !forbiddenIndexes.contains( it ) }
		log.info("Third time lucky!")
	} else {
		log.info("Maybe found second time")
	}
	
	return toChange;
}

def next() {
	if (propagatedIndexes.isEmpty() && !doAfterPropagations.isEmpty()) {
		System.out.println("%%%%%%%% Doing next thing after propagations");
		def todo = doAfterPropagations.remove(0);
		onChangeValue(todo[0], todo[1], todo[2]);
	}
	if (propagatedIndexes.isEmpty() && doAfterPropagations.isEmpty()) {
		log.info( "DONE! termvals = " + terminalProbabilities*.value );
//		for (i in 0 ..< terminalProbabilities.size()) {
//			if (terminalProbabilities[i].value != vals[i]) {
//				terminalProbabilities[i].value = vals[i]
//				break
//			}
//		}
	}
}

def randomizeTerminal() {
	r = random.nextInt( 100 )
	s = 0
	for(entry in terminalProbabilities) {
		p = entry.value.value
		if( s <= r && s+p > r )
			return entry.key
		s += p
	}
	return randomizeTerminal() //in case no terminal matched because of rounding errors, we try it again
}

createProperty( 'type', String, "Round-Robin" ) {
	refreshLayout()
}

slider = new Slider(min: 2, max: 10, majorTickUnit:1, minorTickCount:0, showTickLabels: true, snapToTicks: true, showTickMarks: true)
invalidator = { if(!slider.valueChanging) numOutputs.value = slider.value } as InvalidationListener
slider.valueChangingProperty().addListener( invalidator )
slider.valueProperty().addListener( invalidator )

createProperty( 'numOutputs', Integer, 2 ) { outputCount ->

	while ( outgoingTerminalList.size() < outputCount ) {
		createOutgoing()
		def i = outgoingTerminalList.size() - 1

		countDisplays[i] = { counters["output_$i"].get() - resetValues[i] }

		initialValue = outgoingTerminalList.size() > 1 ? 0 : 100
		log.info("Adding new terminal, current history: " + userChanges)

		createProbabilityProperty(i)
	}

	while ( outgoingTerminalList.size() > outputCount ) {
		def i = outgoingTerminalList.size() - 1
		deleteOutgoing()
		countDisplays.remove( i )?.release()
		userChanges -= i
		deleteProperty( terminalProbabilities.remove( i )?.key )
		
		if (vals[i] > 0) {
			vals[i] = 0
			compensateAfterChange( [ i ], sum( vals ) )
		}
		
	}

	slider.value = outputCount
	refreshLayout()

}


lastOutput = -1

onMessage = { outgoing, incoming, message ->
	if( incoming == incomingTerminal ) {
		if ( type.value == "Round-Robin" ) lastOutput = (lastOutput + 1) % numOutputs.value
		else lastOutput = randomizeTerminal()
		send( outgoingTerminalList[lastOutput], message )
		counters["output_$lastOutput"].increment()
		total.increment()
	}
}

onAction( "RESET" ) {
	lastOutput = -1
	resetValues = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ]
}

refreshLayout = {
	layout ( layout:'gap 10 5' ) {
		node( widget: 'selectorWidget', label: "Type", labels: [ "Round-Robin", "Random" ], default: type.value, selected: type )
		separator( vertical: true )
		box( layout: 'wrap, ins 0' ) {
			label( 'Number of Outputs' )
			node( component: slider, constraints: 'center, w 270!' )
		}

		separator( vertical: true )
		box( layout: 'wrap, ins 0' ) {
			box( widget: 'display',  constraints: 'w 100!' ) {
				node( label: 'Count', content: { total.get() }, constraints: 'wrap' )
			}
		}
		separator( vertical: false )
		box( layout: 'ins 0, center', constraints: 'span 5, w 498!' ) {
			def gap = (int)((249/numOutputs.value)-19)
			def moreThanOneTerminal = numOutputs.value > 1;
			for( i=0; i < outgoingTerminalList.size(); i++ ) {
				if( i != 0 ) separator( vertical: true )

				if( type.value == "Random" ) {
					property( property:terminalProbabilities[i], label:'%', min: 0, max: 100, step: 1, enabled:moreThanOneTerminal, layout: 'ins -15, center', constraints: "w 32!, gap "+gap+" "+gap )
				}
				else {
					box( widget: 'display', layout: 'ins -5, center', constraints: "w 50!, h 24!, gap "+gap+" "+gap ) {
						node( content: countDisplays[i], constraints: 'pad -6 -4' )
					}
				}
			}
		}
	}
}

compactLayout {
	box( widget: 'display', layout: 'wrap, fillx', constraints: 'growx' ) {
		node( label: 'Count', content: { total.get() } )
		node( label: 'Distribution', content: { (0..outgoingTerminalList.size() - 1).collect( { counters["output_$it"].get() - resetValues[it] } ).join( " " ) } )
	}
}