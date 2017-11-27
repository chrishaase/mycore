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

import { Injectable } from '@angular/core';
import { Observable, Observer } from 'rxjs/Rx';

@Injectable()
export class ProcessingService {

    path: string = "/ws/mycore-webtools/processing";

    socketURL: string = null;
    retryCounter: number;

    socket: WebSocket = null;
    observable: Observable<MessageEvent> = null;

    constructor() {
        var loc = window.location;
        var protocol = "ws://";
        if ( location.protocol == "https:" ) {
            protocol = "wss://";
        }
        this.socketURL = protocol + loc.host + this.getBasePath( loc.pathname ) + this.path;
    }

    public send( message: String ) {
        if ( message == "" ) {
            return;
        }
        this.retryCounter++;
        if ( this.socket.readyState === 1 ) {
            this.retryCounter = 0;
            this.socket.send( message );
            return;
        }
        if ( this.socket == undefined || this.socket.readyState === 3 ) {
            if ( this.retryCounter < 5 ) {
                this.connect();
                this.send( message );
            }
            return;
        }
        if ( this.socket.readyState === 0 || this.socket.readyState === 2 ) {
            if ( this.retryCounter < 5 ) {
                setTimeout(() => this.send( message ), 500 );
            }
            return;
        }
    }

    private getBasePath( path: String ) {
        var pathArray = location.pathname.split( "/" )
        pathArray.splice( -4 );
        return pathArray.join( "/" );
    }

    public connect() {
        this.retryCounter = 0;
        this.socket = new WebSocket( this.socketURL );

        this.observable = Observable.create(( observer: Observer<MessageEvent> ) => {
            this.socket.onmessage = observer.next.bind( observer );
            this.socket.onerror = observer.error.bind( observer );
            this.socket.onclose = observer.complete.bind( observer );
            return this.socket.close.bind( this.socket );
        });

        this.socket.onopen = () => {
            var message = {
                type: "connect"
            }
            this.send( JSON.stringify( message ) );
        }
    }

}
