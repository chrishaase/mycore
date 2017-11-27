/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

import { Component, ViewChild, ViewEncapsulation } from '@angular/core';

import { CollectionComponent } from './collection.component';
import { ProcessingService } from './../service/processing.service';
import { Registry, Collection, Processable } from '../model/model';

import { Subject } from 'rxjs/Rx';

@Component( {
    selector: 'processing',
    templateUrl: 'html/app.html',
    styleUrls: ['css/app.css'],
    encapsulation: ViewEncapsulation.None,
    providers: [ProcessingService]
})
export class AppComponent {

    errorCode: number;

    registry: Registry;

    constructor( private processingService: ProcessingService ) {
        this.connect();
    }

    connect() {
        this.processingService.connect();
        this.processingService.observable.subscribe(
            ( me: MessageEvent ) => this.handleSubscription( me ),
            ( error: any ) => this.handleError( error ),
            () => this.handleDone()
        );
    }

    handleSubscription( messageEvent: MessageEvent ) {
        this.handleMessage( JSON.parse( messageEvent.data ) );
    }

    handleError( error: any ) {
        console.log( error );
    }

    handleDone() {
        this.connect();
    }

    handleMessage( data: any ) {
        let dataType = data.type;

        if ( dataType == "error" ) {
            this.errorCode = parseInt( data.error );
            return;
        }
        if ( dataType == "registry" ) {
            this.errorCode = null;
            this.registry = new Registry();
        }
        if ( dataType == "addCollection" ) {
            let collection: Collection = new Collection();
            this.mixin( data, collection );
            collection.updatePropertyKeys();
            this.registry.collections.push( collection );
        }
        if ( dataType == "addProcessable" ) {
            let processable: Processable = new Processable();
            this.mixin( data, processable );
            let collection = this.registry.getCollection( data.collectionId );
            if ( collection == null ) {
                console.log( "Unable to find collection with id " + data.collectionId );
                return;
            }
            collection.processables.push( processable );
        }
        if ( dataType == "updateProcessable" ) {
            let processable: Processable = this.registry.getProcessable( data.id );
            if ( processable == null ) {
                console.log( "Unable to find processable with id " + data.id );
                return;
            }
            this.mixin( data, processable );
        }
        if ( dataType == "removeProcessable" ) {
            let oldProcessable: Processable = this.registry.removeProcessable( data.id );
            if ( oldProcessable == null ) {
                console.log( "Unable to remove processable with id " + data.id + " cause its not in any collection." );
            }
        }
        if( dataType == "updateCollectionProperty") {
            let collection: Collection = this.registry.getCollection(data.id);
            if(collection == null) {
                console.log("Unable to find collection with id " + data.id);
                return;
            }
            collection.setProperty(data.propertyName, data.propertyValue);
        }
    }

    mixin( source: any, target: any ) {
        for ( let prop in source ) {
            if ( source.hasOwnProperty( prop ) ) {
                target[prop] = source[prop];
            }
        }
    }

}
