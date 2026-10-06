# Admin Guide

## Admin Dashboard

The admin dashboard provides system-wide visibility into complaint management.

### Summary Report
- **Status Counts:** Complaints grouped by status
- **Overdue Count:** Complaints past their SLA due date
- **Average Resolution Time:** Hours from creation to resolution
- **Average Feedback Rating:** User satisfaction score (1-5 stars)

### Trend Report
- 30-day view of complaints created vs. resolved
- Helps identify workload patterns and bottlenecks
- Accessible via `/admin/reports/trend`

## Managing Complaints

### Assigning Complaints

1. Go to "All Complaints"
2. Find an unassigned or misassigned complaint
3. Click "Assign"
4. Select the staff member from the dropdown
5. Confirm the assignment
6. The staff member receives a notification

### Reassigning Complaints

1. Open the complaint
2. Click "Reassign"
3. Select a different staff member
4. The system logs this as a REASSIGNED action

### Managing Status Transitions

Only valid transitions are allowed:
- SUBMITTED → ASSIGNED (admin assigns)
- ASSIGNED → IN_PROGRESS (staff starts work)
- IN_PROGRESS → RESOLVED (staff with summary)
- RESOLVED → CLOSED (admin/staff)
- RESOLVED → REOPENED (admin/complainant)
- REOPENED → ASSIGNED or IN_PROGRESS

Invalid transitions are rejected with a clear error message.

### Escalation

**Automatic Escalation:**
- Scheduled job runs every 15 minutes
- Checks for complaints past their due date
- Automatically escalates once
- Admin dashboard highlights overdue complaints

**Manual Actions:**
- Admins can reassign to prevent escalation
- Increase priority if needed
- Add remarks with next steps

## Managing System Configuration

### Categories

1. Go to "Categories"
2. **Create:** Click "New Category", enter name and description
3. **Edit:** Click on existing category, modify fields
4. **Deactivate:** Toggle "Active" to disable (complaints retain existing category)

Use clear, descriptive names for better organization.

### Priorities

1. Go to "Priorities"
2. **Create:** Click "New Priority"
   - Enter name (e.g., "Critical")
   - Set level (1-4, higher = more urgent)
   - Set SLA hours (time to resolve)
3. **Edit:** Modify name, level, or SLA hours
4. **Deactivate:** Disable if no longer used

SLA hours calculate the due date automatically when a complaint is created.

### Users

1. Go to "Users"
2. **View All:** See all registered users with their roles
3. **Change Role:**
   - Select user
   - Choose role (COMPLAINANT, STAFF, ADMIN)
   - Confirm change
4. **Activate/Deactivate:**
   - Toggle active status
   - Deactivated users cannot log in

## Reports & Analytics

### Summary Report
- Total complaints by status
- Breakdown by category and priority
- Average resolution metrics
- Feedback ratings

### Trend Report
- Daily complaint creation rate
- Daily complaint resolution rate
- 30-day historical view
- Customizable date range

Use reports for:
- Performance monitoring
- Workload planning
- SLA compliance tracking
- User satisfaction trends

## Best Practices

1. **Regular Assignment Review**
   - Monitor staff workload
   - Balance assignments fairly
   - Reassign if someone is overloaded

2. **Category Management**
   - Keep categories relevant
   - Archive old categories
   - Use clear naming conventions

3. **Priority Setting**
   - Align SLA hours with business needs
   - Communicate SLA expectations
   - Use escalation as a signal for process improvement

4. **Staff Supervision**
   - Review overdue complaints regularly
   - Provide coaching and feedback
   - Celebrate resolution achievements

5. **User Management**
   - Deactivate users who leave
   - Update roles as responsibilities change
   - Maintain clear access control

## Troubleshooting

### Complaints Not Escalating?
- Check if complaint is past due_at
- Verify scheduler is running (`@Scheduled` job every 15 min)
- Check logs for errors

### Cannot Assign Complaint?
- Verify complaint status allows assignment (SUBMITTED/REOPENED)
- Ensure selected staff member is active
- Check permission level

### Reports Showing Zero?
- Ensure complaints exist with appropriate filters
- Check date ranges
- Verify database connection

## System Health

Monitor:
- Database connection and size
- API response times
- Notification delivery
- File upload storage
- Server CPU/memory usage
- JWT token expiry issues

Check logs regularly for:
- Authentication failures
- API errors
- Database issues
- File upload problems
