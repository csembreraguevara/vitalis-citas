import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', children: [] }]),
      ],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('empieza sin sesión', () => {
    expect(service.autenticado()).toBe(false);
    expect(service.token()).toBeNull();
  });

  it('guarda la sesión al iniciar sesión correctamente', () => {
    service.login({ username: 'admin', password: 'Vitalis2026' }).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({
      token: 'abc.def.ghi',
      tipo: 'Bearer',
      expiraEnSegundos: 3600,
      username: 'admin',
      rol: 'ADMIN',
    });

    expect(service.autenticado()).toBe(true);
    expect(service.token()).toBe('abc.def.ghi');
    expect(service.usuario()?.rol).toBe('ADMIN');
  });

  it('no guarda sesión si las credenciales son inválidas', () => {
    service.login({ username: 'admin', password: 'mala' }).subscribe({ error: () => undefined });

    http
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush(
        { mensaje: 'Usuario o contraseña incorrectos' },
        { status: 401, statusText: 'Unauthorized' },
      );

    expect(service.autenticado()).toBe(false);
  });

  it('cierra la sesión con logout', () => {
    service.login({ username: 'admin', password: 'Vitalis2026' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({
      token: 't',
      tipo: 'Bearer',
      expiraEnSegundos: 3600,
      username: 'admin',
      rol: 'ADMIN',
    });

    service.logout();

    expect(service.autenticado()).toBe(false);
  });
});
