import {Component, OnInit, OnDestroy} from "@angular/core";
import {AppService} from "../app.service";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/fromEvent";
import {Subscription} from "rxjs/Subscription";

import {EurekaService, EurekaServiceInstance} from './eurekaservice.model';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/bootstrap';
import {ViewContainerRef} from "@angular/core";

@Component({
    selector: 'app-eurekaservice',
    templateUrl: './eurekaservice.component.html',
    styleUrls: ['./eurekaservice.component.css']
})

export class EurekaServiceComponent implements OnInit, OnDestroy {

    private eurekaServices: EurekaService[];
    //private eurekaServices: EurekaService[];

    private currentEurekaService: string;
    private showSettings: boolean = false;
    private webSocket: WebSocket;
    private subscription: Subscription;

    constructor(private appService: AppService, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        overlay.defaultViewContainer = vcRef;
    }

    ngOnInit() {
        // GET SERVICES INITIALLY FROM API
        this.appService.getEurekaServices().subscribe(eurekaServices => {
           console.log('* Initial load of Services from API');
           console.dir(eurekaServices);

           this.eurekaServices = eurekaServices['services'];

           // SETUP WEBSOCKET TO LISTEN FOR SERVICES
           this.webSocket = new WebSocket('ws://' + window.location.hostname + ':9110/ws/services');

           this.webSocket.onopen = function(){
               console.log('* Services Connection open!');
           }

           this.subscription = Observable.fromEvent(this.webSocket, 'message').subscribe(services => {
               console.log('* Services Connection message');
               console.dir(services);
               let tmpData = JSON.parse(services['data']);
               this.eurekaServices = tmpData.services;
           });
         });

        // LISTEN FOR CHANGE IN DISPLAYSETTINGS. THIS COULD ALSO BE TRIGGERED FROM
        // THE SETTINGS COMPONENT AS WELL AS FROM HERE.
        this.appService.displaySettings.subscribe(boolValue => {
            this.showSettings = boolValue;
        });
    }

    ngOnDestroy(): void {
        if(this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    refreshData(): void {
        this.appService.getEurekaServices().subscribe(eurekaServices => {
           console.log('* Refresh load of Services from API');
           console.dir(eurekaServices);
           this.eurekaServices = eurekaServices['services'];
        });
    }

    showSettingsInfo(): void {
        this.appService.toggleSettings(!this.showSettings);
    }


    confirmAction(eurekaInstance, appServiceMethod, title): void {
        this.modal.confirm()
            //.size('sm')
            .isBlocking(true)
            .showClose(true)
            .keyboard(27)
            .title(title)
            .body(eurekaInstance.app + ' / ' + eurekaInstance.instanceId)
            .open()
            .then((dialog: any) => { return dialog.result })
            .then((result: any) => { 
                console.log('RESULT: ' + result); 
                // FORM APPROPRIATE APPSERVICE CALL
                this.appService[appServiceMethod](eurekaInstance).subscribe(returnValue => {
                    console.log('API Result: ' + returnValue);
                });    
            })
            .catch((err: any) => { /* DO NOTHING */ });
    }

    kill(eurekaInstance): void {
        if (eurekaInstance) {
            this.confirmAction(eurekaInstance, 'killEurekaService', 'Kill Service');
        }
    }

    load(eurekaInstance): void {
        if (eurekaInstance) {
            this.confirmAction(eurekaInstance, 'loadEurekaService', 'Apply Load Service');
        }
    }

    exception(eurekaInstance): void {
        if (eurekaInstance) {
            this.confirmAction(eurekaInstance, 'exceptionEurekaService', 'Invoke Exception on');
        }
    }

    memory(eurekaInstance): void {
        if (eurekaInstance) {
            this.confirmAction(eurekaInstance, 'memoryEurekaService', 'Grow Memory on');
        }
    }


}
