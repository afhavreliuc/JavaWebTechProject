import { useState, useEffect } from 'react';
import { patientService, medicalServiceService, appointmentService } from '../services/api';
import { Calendar, Clock, CheckCircle2, AlertCircle, Edit2, Trash2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

export default function AppointmentsPage() {
  const { user } = useAuth();
  const [patients, setPatients] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [patientAppointments, setPatientAppointments] = useState([]);
  const [editingAppointment, setEditingAppointment] = useState(null);
  
  const [bookingMode, setBookingMode] = useState(false);
  const [bookingData, setBookingData] = useState({
    medicalServiceId: '',
    date: new Date().toISOString().split('T')[0],
    timeSlot: ''
  });
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [searchPerformed, setSearchPerformed] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (user.role === 'DOCTOR') {
          const pRes = await patientService.getAll();
        setPatients(pRes.data);
        }
        
        const sRes = await medicalServiceService.getAll();
        // Filtrăm doar serviciile care au cel puțin un doctor alocat
        const servicesWithDoctors = sRes.data.filter(s => s.medicalServiceDoctors && s.medicalServiceDoctors.length > 0);
        setServices(servicesWithDoctors);
        
        if (user.role === 'PATIENT' && user.patientId) {
          setSelectedPatientId(user.patientId);
        }
      } catch (error) {
        console.error('Error fetching initial data:', error);
      }
    };
    fetchData();
  }, [user]);

  useEffect(() => {
    if (user.role === 'PATIENT' && selectedPatientId) {
      fetchPatientAppointments(selectedPatientId);
    } else if (user.role === 'DOCTOR') {
      if (selectedPatientId) {
        // Dacă un medic a selectat un pacient specific, vedem programările acelui pacient (filtrate de backend pentru acest medic)
        fetchPatientAppointments(selectedPatientId);
      } else if (user.doctorId) {
        // Dacă nu e selectat niciun pacient, medicul vede toate programările sale
        fetchDoctorAppointments(user.doctorId);
      }
    } else {
      setPatientAppointments([]);
    }
  }, [selectedPatientId, user]);

  const fetchPatientAppointments = async (id) => {
    try {
      const res = await appointmentService.getByPatientId(id);
      setPatientAppointments(res.data);
    } catch (error) {
      console.error('Error fetching appointments:', error);
    }
  };

  const fetchDoctorAppointments = async (id) => {
    try {
      const res = await appointmentService.getByDoctorId(id);
      setPatientAppointments(res.data);
    } catch (error) {
      console.error('Error fetching doctor appointments:', error);
    }
  };

  const handleFetchSlots = async (serviceId, date) => {
    const sId = serviceId || bookingData.medicalServiceId;
    const d = date || bookingData.date;
    if (!sId || !d) return;
    setLoadingSlots(true);
    setSearchPerformed(true);
    try {
      const res = await appointmentService.getAvailableSlots(sId, d);
      setAvailableSlots(res.data);
    } catch (error) {
      console.error('Error fetching slots:', error);
    } finally {
      setLoadingSlots(false);
    }
  };

  const refreshAppointments = () => {
    if (user.role === 'PATIENT' && selectedPatientId) {
      fetchPatientAppointments(selectedPatientId);
    } else if (user.role === 'DOCTOR') {
      if (selectedPatientId) {
        fetchPatientAppointments(selectedPatientId);
      } else if (user.doctorId) {
        fetchDoctorAppointments(user.doctorId);
      }
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Ești sigur că vrei să ștergi această programare?')) return;
    try {
      await appointmentService.delete(id);
      setMessage({ type: 'success', text: 'Programare ștearsă cu succes!' });
      refreshAppointments();
    } catch (error) {
      console.error('Error deleting appointment:', error);
      setMessage({ 
        type: 'error', 
        text: 'Nu s-a putut șterge programarea. Aceasta poate avea plăți procesate sau alte restricții.' 
      });
    }
  };

  const handleEditClick = (appt) => {
    setEditingAppointment(appt);
    setBookingData({
      medicalServiceId: appt.medicalService.id,
      date: new Date(appt.appointmentFrom).toISOString().split('T')[0],
      timeSlot: appt.appointmentFrom
    });
    setBookingMode(true);
    handleFetchSlots(appt.medicalService.id, new Date(appt.appointmentFrom).toISOString().split('T')[0]);
  };

  const handleBook = async () => {
    if (!selectedPatientId || !bookingData.timeSlot) return;
    try {
      if (editingAppointment) {
        await appointmentService.update(editingAppointment.id, bookingData.timeSlot);
        setMessage({ type: 'success', text: 'Programare actualizată cu succes!' });
      } else {
        await appointmentService.create({
          patientId: selectedPatientId,
          medicalServiceId: bookingData.medicalServiceId,
          appointmentFrom: bookingData.timeSlot
        });
        setMessage({ type: 'success', text: 'Programare creată cu succes!' });
      }
      setBookingMode(false);
      setEditingAppointment(null);
      setSearchPerformed(false);
      refreshAppointments();
    } catch (error) {
      console.error('Error booking/updating appointment:', error);
      const backendError = typeof error.response?.data === 'string' ? error.response.data : null;
      setMessage({ 
        type: 'error', 
        text: backendError || (editingAppointment ? 'Eroare la actualizarea programării.' : 'Eroare la crearea programării. Slotul s-ar putea să nu mai fie disponibil.') 
      });
    }
  };

  const formatDateTime = (isoString) => {
    const d = new Date(isoString);
    return d.toLocaleString('ro-RO', { 
      day: '2-digit', month: '2-digit', year: 'numeric', 
      hour: '2-digit', minute: '2-digit' 
    });
  };

  return (
    <div className="w-full">
      <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-100">
        <h2 className="text-3xl font-bold text-gray-800 mb-6 flex items-center gap-3">
          <Calendar className="text-indigo-600" size={32} />
          Centru de Programări
        </h2>

        {message && (
          <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 ${message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
            {message.type === 'success' ? <CheckCircle2 /> : <AlertCircle />}
            {message.text}
          </div>
        )}

        <div className="mb-8 p-4 bg-indigo-50 rounded-xl">
          <label className="block text-sm font-bold text-indigo-900 mb-2">
            {user.role === 'DOCTOR' ? 'Filtrează după Pacient' : 'Selectează Pacientul'} <span className="text-blue-600">*</span>
          </label>
          <select 
            className="w-full border-none rounded-lg p-3 shadow-sm bg-white focus:ring-2 focus:ring-indigo-500"
            value={selectedPatientId}
            onChange={(e) => setSelectedPatientId(e.target.value)}
            disabled={user.role === 'PATIENT'}
          >
            {user.role === 'DOCTOR' ? (
              <option value="">Toți pacienții (Programările Mele)</option>
            ) : (
              <option value="">Alege un pacient din listă...</option>
            )}
            {patients.map(p => <option key={p.id} value={p.id}>{p.name} (ID: {p.id})</option>)}
          </select>
        </div>

        {(selectedPatientId || user.role === 'DOCTOR') && (
          <div>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold text-gray-700">
                {user.role === 'DOCTOR' ? 'Programările Mele (Medic)' : 'Programările Pacientului'}
              </h3>
              {!bookingMode && user.role === 'PATIENT' && (
                <button 
                  onClick={() => setBookingMode(true)}
                  className="bg-indigo-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-indigo-700 transition-all shadow-md"
                >
                  Programează Vizită Nouă
                </button>
              )}
            </div>

            {bookingMode ? (
              <div className="bg-gray-50 p-6 rounded-xl border-2 border-dashed border-indigo-200 animate-in fade-in slide-in-from-top-4 duration-300">
                <h4 className="text-lg font-bold text-indigo-900 mb-4">
                  {editingAppointment ? `Editare Programare #${editingAppointment.id}` : 'Programare Nouă'}
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-semibold text-gray-600 mb-2">
                      Serviciu Medical <span className="text-blue-600">*</span>
                    </label>
                    <select 
                      className="w-full border p-3 rounded-lg bg-white disabled:bg-gray-100"
                      value={bookingData.medicalServiceId}
                      onChange={(e) => {
                        setBookingData({...bookingData, medicalServiceId: e.target.value});
                        setSearchPerformed(false);
                        setAvailableSlots([]);
                      }}
                      disabled={!!editingAppointment}
                    >
                      <option value="">Alege serviciu...</option>
                      {services.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-semibold text-gray-600 mb-2">
                      Data <span className="text-blue-600">*</span>
                    </label>
                    <div className="flex gap-2">
                      <input 
                        type="date" 
                        className="flex-1 border p-3 rounded-lg"
                        value={bookingData.date}
                        min={new Date().toISOString().split('T')[0]}
                        onChange={(e) => {
                          setBookingData({...bookingData, date: e.target.value});
                          setSearchPerformed(false);
                          setAvailableSlots([]);
                        }}
                      />
                      <button 
                        onClick={() => handleFetchSlots()}
                        className="bg-gray-800 text-white px-4 rounded-lg hover:bg-black"
                      >
                        Caută Sloturi
                      </button>
                    </div>
                  </div>
                </div>

                {loadingSlots && <p className="mt-4 text-center text-indigo-600 font-medium">Se caută sloturi libere...</p>}

                {searchPerformed && !loadingSlots && availableSlots.length === 0 && !editingAppointment && (
                  <div className="mt-6 p-4 bg-orange-50 border border-orange-200 rounded-lg flex items-center gap-3 text-orange-700">
                    <AlertCircle size={20} />
                    <p className="font-medium">Nu mai sunt sloturi disponibile pentru această dată. Te rugăm să alegi altă zi.</p>
                  </div>
                )}

                {(availableSlots.length > 0 || (editingAppointment && new Date(editingAppointment.appointmentFrom).toISOString().split('T')[0] === bookingData.date)) && (
                  <div className="mt-6">
                    <label className="block text-sm font-semibold text-gray-600 mb-3">
                      Sloturi disponibile <span className="text-blue-600">*</span>
                    </label>
                    <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-3">
                      {editingAppointment && 
                       new Date(editingAppointment.appointmentFrom).toISOString().split('T')[0] === bookingData.date && 
                       !availableSlots.includes(editingAppointment.appointmentFrom) && (
                        <button
                          onClick={() => setBookingData({...bookingData, timeSlot: editingAppointment.appointmentFrom})}
                          className={`p-3 rounded-lg text-sm font-medium transition-all ${
                            bookingData.timeSlot === editingAppointment.appointmentFrom 
                            ? 'bg-indigo-600 text-white ring-4 ring-indigo-200' 
                            : 'bg-white border border-indigo-200 text-indigo-700'
                          }`}
                        >
                          {new Date(editingAppointment.appointmentFrom).toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })}
                          <span className="block text-[10px] opacity-75">(Curent)</span>
                        </button>
                      )}
                      {availableSlots.map((slot) => (
                        <button
                          key={slot}
                          onClick={() => setBookingData({...bookingData, timeSlot: slot})}
                          className={`p-3 rounded-lg text-sm font-medium transition-all ${
                            bookingData.timeSlot === slot 
                            ? 'bg-indigo-600 text-white ring-4 ring-indigo-200' 
                            : 'bg-white border hover:border-indigo-500 text-gray-700'
                          }`}
                        >
                          {new Date(slot).toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div className="mt-8 flex justify-end gap-3">
                  <button 
                    onClick={() => {setBookingMode(false); setEditingAppointment(null); setAvailableSlots([]); setSearchPerformed(false);}}
                    className="px-6 py-2 text-gray-600 font-semibold hover:bg-gray-200 rounded-full"
                  >
                    Renunță
                  </button>
                  <button 
                    onClick={handleBook}
                    disabled={!bookingData.timeSlot}
                    className="px-8 py-2 bg-indigo-600 text-white font-bold rounded-full shadow-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-indigo-700"
                  >
                    {editingAppointment ? 'Salvează Modificările' : 'Confirmă Programarea'}
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-3">
                {patientAppointments.map((appt) => (
                  <div key={appt.id} className="bg-white p-4 rounded-xl border flex items-center justify-between hover:shadow-sm group">
                    <div className="flex items-center gap-4">
                      <div className="p-3 bg-indigo-50 text-indigo-600 rounded-full">
                        <Clock size={24} />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                        <p className="font-bold text-gray-800">{appt.medicalService?.name}</p>
                          {appt.medicalService?.price && (
                            <span className="px-2 py-0.5 bg-green-100 text-green-700 text-xs font-bold rounded-full">
                              {appt.medicalService.price} RON
                            </span>
                          )}
                        </div>
                        <p className="text-sm text-gray-500">{formatDateTime(appt.appointmentFrom)}</p>
                        {user.role === 'DOCTOR' && <p className="text-xs text-indigo-600 font-medium">Pacient: {appt.patient?.name}</p>}
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <span className={`px-3 py-1 rounded-full text-xs font-bold ${
                          appt.status === 'Completed' ? 'bg-gray-100 text-gray-600' : 'bg-blue-100 text-blue-700'
                        }`}>
                          {appt.status}
                        </span>
                        <p className="text-xs text-gray-400 mt-1">ID: #{appt.id}</p>
                      </div>
                      
                      {appt.status !== 'Completed' && user.role === 'PATIENT' && (
                        <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          <button 
                            onClick={() => handleEditClick(appt)}
                            className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg"
                            title="Editează"
                          >
                            <Edit2 size={18} />
                          </button>
                          <button 
                            onClick={() => handleDelete(appt.id)}
                            className="p-2 text-red-600 hover:bg-red-50 rounded-lg"
                            title="Șterge"
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
                {patientAppointments.length === 0 && (
                  <div className="text-center py-12 border-2 border-dashed rounded-2xl text-gray-400">
                    {user.role === 'DOCTOR' ? 'Nu ai programări viitoare.' : 'Acest pacient nu are programări viitoare.'}
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {!selectedPatientId && user.role !== 'DOCTOR' && (
          <div className="text-center py-20 text-gray-400">
            <Calendar size={64} className="mx-auto mb-4 opacity-20" />
            <p className="text-lg">Selectează un pacient pentru a-i vedea sau crea programări.</p>
          </div>
        )}
      </div>
    </div>
  );
}

